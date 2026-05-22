package id.ac.ui.cs.advprog.groupproject.auth.e2e;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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
class AuthE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String existingEmail = "auth_e2e_existing@example.com";
    private final String existingPassword = "passw0rd";

    @BeforeEach
    void setUp() {
        User existing = new User();
        existing.setUsername("auth_e2e_existing");
        existing.setEmail(existingEmail);
        existing.setPassword(passwordEncoder.encode(existingPassword));
        existing.setRole(Role.ROLE_TITIPER.toString());
        existing.setStatus(Status.ACTIVE.toString());
        userRepository.save(existing);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void registerPageRenders() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            driver.get(baseUrl("/register"));

            assertNotNull(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailInput"))));
            assertNotNull(driver.findElement(By.id("passwordInput")));
            assertNotNull(driver.findElement(By.id("confirmPasswordInput")));
            assertNotNull(driver.findElement(By.id("registerButton")));
        } finally {
            driver.quit();
        }
    }

    @Test
    void registerNewUserRedirectsToLoginWithRegisteredFlag() {
        String newEmail = "auth_e2e_new@example.com";
        String newPassword = "newpassword";

        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            driver.get(baseUrl("/register"));

            driver.findElement(By.id("emailInput")).sendKeys(newEmail);
            driver.findElement(By.id("passwordInput")).sendKeys(newPassword);
            driver.findElement(By.id("confirmPasswordInput")).sendKeys(newPassword);
            driver.findElement(By.id("usernameInput")).sendKeys("auth_e2e_new");
            driver.findElement(By.id("fullName")).sendKeys("Auth E2E New");
            driver.findElement(By.id("registerButton")).click();

            wait.until(ExpectedConditions.urlContains("/login"));
            assertTrue(driver.getCurrentUrl().contains("registered"),
                    "Successful registration must redirect to /login?registered");
            assertTrue(userRepository.findByEmail(newEmail).isPresent(),
                    "Newly registered user should be persisted");
        } finally {
            driver.quit();
        }
    }

    @Test
    void registerWithMismatchedPasswordsShowsErrorAndDoesNotPersist() {
        String email = "auth_e2e_mismatch@example.com";

        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            driver.get(baseUrl("/register"));

            driver.findElement(By.id("emailInput")).sendKeys(email);
            driver.findElement(By.id("passwordInput")).sendKeys("password1");
            driver.findElement(By.id("confirmPasswordInput")).sendKeys("password2");
            driver.findElement(By.id("registerButton")).click();

            wait.until(ExpectedConditions.urlContains("/register"));
            assertTrue(driver.getCurrentUrl().contains("error"),
                    "Mismatched passwords must redirect back with ?error");
            assertTrue(userRepository.findByEmail(email).isEmpty(),
                    "User must not be persisted when passwords mismatch");
        } finally {
            driver.quit();
        }
    }

    @Test
    void registerWithExistingEmailShowsUserExistsFlag() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            driver.get(baseUrl("/register"));

            driver.findElement(By.id("emailInput")).sendKeys(existingEmail);
            driver.findElement(By.id("passwordInput")).sendKeys("anotherpass");
            driver.findElement(By.id("confirmPasswordInput")).sendKeys("anotherpass");
            driver.findElement(By.id("registerButton")).click();

            wait.until(ExpectedConditions.urlContains("/register"));
            assertTrue(driver.getCurrentUrl().contains("userExists"),
                    "Duplicate email must redirect with ?userExists flag");
        } finally {
            driver.quit();
        }
    }

    @Test
    void loginWithValidCredentialsRedirectsToHomepage() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, existingEmail, existingPassword);

            assertTrue(driver.getCurrentUrl().contains("/homepage"),
                    "Successful login must land on /homepage");
            assertNotNull(driver.findElement(By.id("logoutButton")),
                    "Logout button should be visible in navbar after login");
        } finally {
            driver.quit();
        }
    }

    @Test
    void loginWithInvalidCredentialsShowsErrorFlag() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            driver.get(baseUrl("/login"));

            driver.findElement(By.name("username")).sendKeys(existingEmail);
            driver.findElement(By.name("password")).sendKeys("wrongpassword");
            driver.findElement(By.id("loginButton")).click();

            wait.until(ExpectedConditions.urlContains("/login"));
            assertTrue(driver.getCurrentUrl().contains("error"),
                    "Failed login must redirect with ?error flag");
        } finally {
            driver.quit();
        }
    }

    @Test
    void logoutRedirectsToLoginWithLogoutFlag() {
        WebDriver driver = createDriver();
        try {
            WebDriverWait wait = createWait(driver);
            login(driver, wait, existingEmail, existingPassword);

            driver.findElement(By.id("logoutButton")).click();

            wait.until(ExpectedConditions.urlContains("/login"));
            assertTrue(driver.getCurrentUrl().contains("logout"),
                    "Logout must redirect with ?logout flag");
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

    private void login(WebDriver driver, WebDriverWait wait, String username, String rawPassword) {
        driver.get(baseUrl("/login"));
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(rawPassword);
        driver.findElement(By.id("loginButton")).click();
        wait.until(ExpectedConditions.urlContains("/homepage"));
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
