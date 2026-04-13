package id.ac.ui.cs.advprog.groupproject.config;

import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInit implements CommandLineRunner {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder encoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public AdminInit(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("andrew.wanarahardja@ui.ac.id").isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setUsername("admin");
            admin.setPassword(encoder.encode(adminPassword));
            admin.setRole(Role.ROLE_ADMIN.toString());
            userRepository.save(admin);
        }
    }
}
