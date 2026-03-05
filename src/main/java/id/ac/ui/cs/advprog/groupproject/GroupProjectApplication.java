package id.ac.ui.cs.advprog.groupproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EntityScan("id.ac.ui.cs.advprog.groupproject.model")
@EnableJpaRepositories("id.ac.ui.cs.advprog.groupproject.repository")
public class GroupProjectApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
        
        SpringApplication app = new SpringApplication(GroupProjectApplication.class);
        
        String defaultProfile = dotenv.get("SPRING_PROFILES_ACTIVE");
        if (defaultProfile != null && !defaultProfile.isEmpty()) {
            app.setDefaultProperties(java.util.Collections.singletonMap(
                "spring.profiles.default", defaultProfile
            ));
        }
        app.run(args);
    }
}
