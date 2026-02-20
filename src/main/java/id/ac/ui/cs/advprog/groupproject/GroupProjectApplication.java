package id.ac.ui.cs.advprog.groupproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("id.ac.ui.cs.advprog.groupproject.model")
@EnableJpaRepositories("id.ac.ui.cs.advprog.groupproject.repository")
public class GroupProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(GroupProjectApplication.class, args);
    }
}
