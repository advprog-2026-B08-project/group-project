package id.ac.ui.cs.advprog.groupproject.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            String profileFromEnv = dotenv.get("SPRING_PROFILES_ACTIVE");
            if (profileFromEnv != null && !profileFromEnv.isEmpty()) {
                if (environment.getActiveProfiles().length == 0) {
                    environment.setActiveProfiles(profileFromEnv);
                    System.out.println("✓ Active profile from .env: " + profileFromEnv);
                }
            }

            Map<String, Object> dotenvProperties = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                dotenvProperties.put(entry.getKey(), entry.getValue());
            });

            environment.getPropertySources()
                    .addLast(new MapPropertySource("dotenvProperties", dotenvProperties));

        } catch (Exception e) {
            System.out.println("Warning: Could not load .env file - using default configuration");
        }
    }
}
