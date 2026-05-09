package id.ac.ui.cs.advprog.groupproject.wallet.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

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

import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;
import io.github.bonigarcia.wdm.WebDriverManager;

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
class WalletE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String email = "e2e_user@example.com";
    private final String password = "password123";

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("e2euser");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ROLE_TITIPER.toString());
        user.setStatus(Status.ACTIVE.toString());
        userRepository.save(user);

        walletService.createWallet(user.getId());
    }

    @AfterEach
    void tearDown() {
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void loginAndOpenWalletPage() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait);
            openWallet(driver, wait);

            WebElement topUpForm = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("topup-form")));

            assertTrue(topUpForm.isDisplayed());
        } finally {
            driver.quit();
        }
    }

    @Test
    void topUpShowsPendingStatusAndHistory() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait);
            openWallet(driver, wait);

            WebElement amountInput = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("topup-amount")));
            amountInput.clear();
            amountInput.sendKeys("10000");

            WebElement submitButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("topup-submit")));
            submitButton.click();

            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("topup-status"), "Top-up"));
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("topup-status"), "PENDING"));
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("history-list"), "TOP_UP"));

            String statusText = driver.findElement(By.id("topup-status")).getText();
            assertTrue(statusText.contains("PENDING"));
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
        return new WebDriverWait(driver, Duration.ofSeconds(6));
    }

    private void login(WebDriver driver, WebDriverWait wait) {
        driver.get(baseUrl("/login"));
        driver.findElement(By.name("username")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.id("loginButton")).click();
        wait.until(ExpectedConditions.urlContains("/homepage"));
    }

    private void openWallet(WebDriver driver, WebDriverWait wait) {
        driver.get(baseUrl("/wallet"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("topup-form")));
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
