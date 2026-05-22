package id.ac.ui.cs.advprog.groupproject.catalog.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
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
class CatalogE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String titiperEmail = "e2e_titiper@example.com";
    private final String titiperPassword = "password123";
    private final String jastiperEmail = "e2e_jastiper@example.com";
    private final String jastiperPassword = "password123";

    private User jastiper;

    @BeforeEach
    void setUp() {
        User titiper = newUser("e2e-titiper", titiperEmail, titiperPassword, Role.ROLE_TITIPER);
        userRepository.save(titiper);

        jastiper = newUser("e2e-jastiper", jastiperEmail, jastiperPassword, Role.ROLE_JASTIPER);
        jastiper = userRepository.save(jastiper);

        catalogRepository.save(buildCatalog("Baju Bola Madrid", "Jersey Real Madrid 2025", 350_000.0, 8, "Madrid"));
        catalogRepository.save(buildCatalog("Cokelat Belgian Premium", "Praline assortment", 250_000.0, 12, "Brussels"));
        catalogRepository.save(buildCatalog("Tas Ransel Tokyo", "Limited edition canvas backpack", 480_000.0, 5, "Tokyo"));
    }

    @AfterEach
    void tearDown() {
        catalogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void catalogPageLoadsCardsViaAjax() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, titiperEmail, titiperPassword);
            openCatalog(driver, wait);

            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                    By.cssSelector(".catalog-card[data-catalog-id]"), 0));

            List<WebElement> cards = driver.findElements(
                    By.cssSelector(".catalog-card[data-catalog-id]"));
            assertEquals(3, cards.size(), "Expected 3 seeded catalogs to render");

            WebElement loading = driver.findElement(By.id("catalog-loading"));
            assertFalse(loading.isDisplayed(), "Loading indicator should be hidden after AJAX completes");
        } finally {
            driver.quit();
        }
    }

    @Test
    void searchFiltersCardsThroughAjax() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, titiperEmail, titiperPassword);
            openCatalog(driver, wait);

            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                    By.cssSelector(".catalog-card[data-catalog-id]"), 0));

            WebElement searchInput = driver.findElement(By.id("catalogSearchInput"));
            searchInput.clear();
            searchInput.sendKeys("Cokelat");

            WebElement searchButton = driver.findElement(By.cssSelector("#catalog-search-form button[type='submit']"));
            searchButton.click();

            wait.until(driverInstance -> {
                List<WebElement> visibleCards = driverInstance.findElements(
                        By.cssSelector(".catalog-card[data-catalog-id]"));
                return visibleCards.size() == 1
                        && visibleCards.get(0).getText().contains("Cokelat");
            });

            // Explicit assertion so static analyzers see the verification step.
            List<WebElement> finalCards = driver.findElements(
                    By.cssSelector(".catalog-card[data-catalog-id]"));
            assertEquals(1, finalCards.size(), "Search should narrow grid to exactly one match");
            assertTrue(finalCards.get(0).getText().contains("Cokelat"),
                    "Remaining card must match the search keyword");
        } finally {
            driver.quit();
        }
    }

    @Test
    void resetSearchRestoresAllCards() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, titiperEmail, titiperPassword);
            openCatalog(driver, wait);

            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                    By.cssSelector(".catalog-card[data-catalog-id]"), 0));

            WebElement searchInput = driver.findElement(By.id("catalogSearchInput"));
            searchInput.clear();
            searchInput.sendKeys("Madrid");
            driver.findElement(By.cssSelector("#catalog-search-form button[type='submit']")).click();

            wait.until(driverInstance -> driverInstance
                    .findElements(By.cssSelector(".catalog-card[data-catalog-id]")).size() == 1);

            driver.findElement(By.id("catalog-search-reset")).click();

            wait.until(driverInstance -> driverInstance
                    .findElements(By.cssSelector(".catalog-card[data-catalog-id]")).size() == 3);

            // Explicit assertion so static analyzers see the verification step.
            List<WebElement> restoredCards = driver.findElements(
                    By.cssSelector(".catalog-card[data-catalog-id]"));
            assertEquals(3, restoredCards.size(), "All seeded catalogs should reappear after reset");
        } finally {
            driver.quit();
        }
    }

    @Test
    void clickingCardNavigatesToDetailPage() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, titiperEmail, titiperPassword);
            openCatalog(driver, wait);

            WebElement firstCard = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector(".catalog-card[data-catalog-id]")));
            firstCard.click();

            wait.until(ExpectedConditions.urlContains("/catalog/detail/"));
            assertTrue(driver.getCurrentUrl().contains("/catalog/detail/"));
        } finally {
            driver.quit();
        }
    }

    @Test
    void jastiperSeesEditButtonOnOwnedCatalog() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, jastiperEmail, jastiperPassword);
            openCatalog(driver, wait);

            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                    By.cssSelector(".catalog-card[data-catalog-id]"), 0));

            List<WebElement> editLinks = driver.findElements(
                    By.cssSelector(".catalog-card a[href*='/catalog/edit/']"));
            assertTrue(editLinks.size() >= 1, "Jastiper should see Edit link on owned catalogs");
        } finally {
            driver.quit();
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
        return new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    private void login(WebDriver driver, WebDriverWait wait, String email, String rawPassword) {
        driver.get(baseUrl("/login"));
        driver.findElement(By.name("username")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys(rawPassword);
        driver.findElement(By.id("loginButton")).click();
        wait.until(ExpectedConditions.urlContains("/homepage"));
    }

    private void openCatalog(WebDriver driver, WebDriverWait wait) {
        driver.get(baseUrl("/catalog"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("catalog-grid")));
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
}
