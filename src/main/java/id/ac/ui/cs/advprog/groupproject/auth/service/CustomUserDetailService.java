package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;

import id.ac.ui.cs.advprog.groupproject.event.UserRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public CustomUserDetailService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                   ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));

        return user;
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    public boolean confirmPassword(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public User createUser(String email, String password, String username, String fullName) {
        String usernameInput = username;
        if (usernameInput == null || usernameInput.isBlank()) {
            usernameInput = getDefaultUsername(email);
        }

        User user = new User();
        user.setUsername(usernameInput);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ROLE_TITIPER.toString());
        user.setStatus(Status.ACTIVE.toString());
        user.setEmail(email);
        if (fullName != null) user.setFullName(fullName);

        userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(this, user.getId()));
        return user;
    }

    public String getDefaultUsername(String email) {
        return email.split("@")[0];
    }

    public Map<String, Long> getUserCountByRole() {
        List<Object[]> results = userRepository.countUsersByRole();
        Map<String, Long> map = new HashMap<>();

        for (Object[] row : results) {
            String role = (String) row[0];
            Long count = (Long) row[1];
            map.put(role, count);
        }
        return map;
    }

    public List<User> getUserList() {
        return userRepository.findAll();
    }

    public void makeStatusPending(User user) {
        User repoUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found!"));
        if (!repoUser.getRole().equals("ROLE_TITIPER")) {
            throw new RuntimeException("Invalid user role!");
        }
        if (!repoUser.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("Invalid user status!");
        }

        repoUser.setStatus(Status.PENDING.toString());
        userRepository.save(repoUser);
    }

    public void demote(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getRole().equals("ROLE_JASTIPER")) {
            throw new RuntimeException("Invalid role for demotion!");
        }

        user.setRole(Role.ROLE_TITIPER.toString());
        userRepository.save(user);

    }

    public void ban(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found!"));
        if (user.getRole().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Invalid role for banning!");
        }

        user.setStatus(Status.BANNED.toString());
        userRepository.save(user);
    }
}

