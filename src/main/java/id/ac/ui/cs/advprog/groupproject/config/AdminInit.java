package id.ac.ui.cs.advprog.groupproject.config;

import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Profile("!test")
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

    @Value("${JASTIPER_EMAIL}")
    private String jastiperEmail;

    @Value("${JASTIPER_PASSWORD}")
    private String jastiperPassword;

    @Value("${TITIPER_EMAIL}")
    private String titiperEmail;

    @Value("${TITIPER_PASSWORD}")
    private String titiperPassword;

    public AdminInit(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setUsername("admin");
            admin.setPassword(encoder.encode(adminPassword));
            admin.setRole(Role.ROLE_ADMIN.toString());
            admin.setStatus(Status.ACTIVE.toString());
            userRepository.save(admin);
        }

        if (userRepository.findByEmail(jastiperEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(jastiperEmail);
            admin.setUsername("jastiper");
            admin.setPassword(encoder.encode(jastiperPassword));
            admin.setRole(Role.ROLE_JASTIPER.toString());
            admin.setStatus(Status.ACTIVE.toString());
            userRepository.save(admin);
        }

        if (userRepository.findByEmail(titiperEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(titiperEmail);
            admin.setUsername("titiper");
            admin.setPassword(encoder.encode(titiperPassword));
            admin.setRole(Role.ROLE_TITIPER.toString());
            admin.setStatus(Status.ACTIVE.toString());
            userRepository.save(admin);
        }
    }
}
