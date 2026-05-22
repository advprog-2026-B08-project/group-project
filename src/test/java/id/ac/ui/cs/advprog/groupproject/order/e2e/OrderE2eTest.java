package id.ac.ui.cs.advprog.groupproject.order.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import id.ac.ui.cs.advprog.groupproject.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.groupproject.order.repository.StatusHistoryRepository;
import id.ac.ui.cs.advprog.groupproject.wallet.model.Wallet;
import id.ac.ui.cs.advprog.groupproject.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.groupproject.wallet.repository.WalletTransactionRepository;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "cloudinary.cloud-name=test-cloud",
            "cloudinary.api-key=test-key",
            "cloudinary.api-secret=test-secret",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.flyway.enabled=false"
        }
)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "E2E", matches = "true")
class OrderE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderE2eTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String titiperEmail = "order_e2e_titiper@example.com";
    private final String titiperPassword = "password123";
    private final String jastiperEmail = "order_e2e_jastiper@example.com";
    private final String jastiperPassword = "password123";

    private User titiper;
    private User jastiper;
    private Catalog product;

    @BeforeEach
    void setUp() {
        titiper = userRepository.save(newUser(
                "order_e2e_titiper", titiperEmail, titiperPassword, Role.ROLE_TITIPER));
        jastiper = userRepository.save(newUser(
                "order_e2e_jastiper", jastiperEmail, jastiperPassword, Role.ROLE_JASTIPER));

        product = catalogRepository.save(buildCatalog(
                "Coklat Belgia", "Premium Belgian chocolates", 100_000.0, 10, "Brussels"));

        walletService.createWallet(titiper.getId());
        walletService.createWallet(jastiper.getId());
        Wallet titiperWallet = walletRepository.findByUserId(titiper.getId()).orElseThrow();
        titiperWallet.setBalance(new BigDecimal("500000"));
        walletRepository.save(titiperWallet);
    }

    @AfterEach
    void tearDown() {
        statusHistoryRepository.deleteAll();
        orderRepository.deleteAll();
        walletTransactionRepository.deleteAll();
        walletRepository.deleteAll();
        catalogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void checkoutFlowCreatesOrderAndDeductsWallet() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, titiperEmail, titiperPassword);

            driver.get(baseUrl("/order/checkout/" + product.getId()));

            WebElement quantity = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("quantity")));
            quantity.clear();
            quantity.sendKeys("2");

            WebElement address = driver.findElement(By.id("shippingAddress"));
            address.sendKeys("Jl. Salemba Raya No. 4, Jakarta");

            // Wait for JS validation to enable the submit button
            wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout-submit")));
            driver.findElement(By.id("checkout-submit")).click();

            wait.until(ExpectedConditions.urlContains("/order/"));
            assertTrue(driver.getCurrentUrl().matches(".*/order/[0-9a-fA-F-]+$"),
                    "Should redirect to order detail page");

            List<Order> orders = orderRepository.findByBuyerId(titiper.getId());
            assertEquals(1, orders.size(), "Exactly one order must be persisted");
            Order order = orders.get(0);
            assertEquals(OrderStatus.PAID, order.getStatus());
            // Use compareTo to avoid BigDecimal scale mismatch
            assertEquals(0, new BigDecimal("200000").compareTo(order.getTotalPrice()),
                    "Total price must be 200000");

            BigDecimal remainingBalance = walletRepository
                    .findByUserId(titiper.getId()).orElseThrow().getBalance();
            assertEquals(0, new BigDecimal("300000").compareTo(remainingBalance),
                    "Wallet should be debited by total price");
        } finally {
            driver.quit();
        }
    }

    @Test
    void orderListShowsActiveTabForTitiper() {
        Order existingOrder = seedPaidOrder(2);

        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, titiperEmail, titiperPassword);
            driver.get(baseUrl("/order/list"));

            WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-panel='active']")));
            assertEquals(1, panel.findElements(
                    By.cssSelector(".order-card[data-order-id='" + existingOrder.getId() + "']"))
                    .size(), "Active tab must show the seeded order");
        } finally {
            driver.quit();
        }
    }

    @Test
    void orderDetailDisplaysShippingAddressAndStatus() {
        Order existingOrder = seedPaidOrder(1);

        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, titiperEmail, titiperPassword);
            driver.get(baseUrl("/order/" + existingOrder.getId()));

            wait.until(ExpectedConditions.urlContains("/order/" + existingOrder.getId()));
            String pageText = driver.findElement(By.tagName("body")).getText();
            assertTrue(pageText.contains("PAID"),
                    "Detail page must render the order status");
            assertTrue(pageText.contains("Jl. Test"),
                    "Detail page must render the shipping address");
        } finally {
            driver.quit();
        }
    }

    @Test
    void cancelOrderRefundsBalanceAndUpdatesStatus() {
        Order existingOrder = seedPaidOrder(1);
        BigDecimal balanceBefore = walletRepository
                .findByUserId(titiper.getId()).orElseThrow().getBalance();

        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, titiperEmail, titiperPassword);
            driver.get(baseUrl("/order/list"));

            WebElement cancelButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".cancel-btn[data-id='" + existingOrder.getId() + "']")));
            cancelButton.click();

            acceptAlert(driver, wait);
            acceptAlert(driver, wait);

            wait.until(ExpectedConditions.urlContains("/order/list"));

            Order refreshed = orderRepository.findById(existingOrder.getId()).orElseThrow();
            assertEquals(OrderStatus.CANCELLED, refreshed.getStatus(),
                    "Order status must transition to CANCELLED");

            BigDecimal balanceAfter = walletRepository
                    .findByUserId(titiper.getId()).orElseThrow().getBalance();
            assertEquals(balanceBefore.add(existingOrder.getTotalPrice()), balanceAfter,
                    "Wallet must be refunded by the order total");
        } finally {
            driver.quit();
        }
    }

    @Test
    void jastiperSeesTodoTabWithPaidOrders() {
        Order existingOrder = seedPaidOrder(1);

        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, jastiperEmail, jastiperPassword);
            driver.get(baseUrl("/order/list"));

            WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-panel='todo']")));
            assertNotNull(panel.findElement(
                    By.cssSelector(".order-card[data-order-id='" + existingOrder.getId() + "']")),
                    "Jastiper to-do tab must list the seeded PAID order");
        } finally {
            driver.quit();
        }
    }

    private Order seedPaidOrder(int quantity) {
        BigDecimal totalPrice = BigDecimal.valueOf(product.getPrice())
                .multiply(BigDecimal.valueOf(quantity));

        Wallet titiperWallet = walletRepository.findByUserId(titiper.getId()).orElseThrow();
        titiperWallet.setBalance(titiperWallet.getBalance().subtract(totalPrice));
        walletRepository.save(titiperWallet);

        Order order = new Order();
        order.setBuyerId(titiper.getId());
        order.setJastiperId(jastiper.getId());
        order.setProductId(product.getId());
        order.setQuantity(quantity);
        order.setShippingAddress("Jl. Test No. 1, Jakarta");
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PAID);
        return orderRepository.save(order);
    }

    private void acceptAlert(WebDriver driver, WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (Exception e) {
            // No alert appeared in time — best-effort handling, continue test.
            LOGGER.debug("acceptAlert: no alert present, continuing. reason={}", e.getMessage());
        }
    }

    private WebDriver createDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1280,720");
        return new ChromeDriver(options);
    }

    private WebDriverWait createWait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    private void login(WebDriver driver, WebDriverWait wait, String email, String rawPassword) {
        driver.get(baseUrl("/login"));
        driver.findElement(By.name("username")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys(rawPassword);
        driver.findElement(By.id("loginButton")).click();
        wait.until(ExpectedConditions.urlContains("/homepage"));
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private User newUser(String username, String email, String rawPassword, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role.toString());
        user.setStatus(Status.ACTIVE.toString());
        return user;
    }

    private Catalog buildCatalog(String name, String description, double price, int stock, String origin) {
        Catalog catalog = new Catalog();
        catalog.setName(name);
        catalog.setDescription(description);
        catalog.setImageUrl("https://example.com/" + name.toLowerCase().replace(' ', '-') + ".jpg");
        catalog.setPrice(price);
        catalog.setStock(stock);
        catalog.setOriginLocation(origin);
        catalog.setTravelDate(LocalDate.now().plusDays(14));
        catalog.setJastiper(jastiper);
        return catalog;
    }

    @SuppressWarnings("unused")
    private UUID parseId(String url) {
        return UUID.fromString(url.substring(url.lastIndexOf('/') + 1));
    }
}
