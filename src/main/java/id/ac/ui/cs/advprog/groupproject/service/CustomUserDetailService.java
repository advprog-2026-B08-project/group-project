package id.ac.ui.cs.advprog.groupproject.service;

import id.ac.ui.cs.advprog.groupproject.model.Role;
import id.ac.ui.cs.advprog.groupproject.model.Status;
import id.ac.ui.cs.advprog.groupproject.model.User;
import id.ac.ui.cs.advprog.groupproject.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .build();
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    public boolean confirmPassword(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public User createUser(String email, String password, String username) {
        String usernameInput = username;
        if (usernameInput == null || usernameInput.isBlank()) {
            usernameInput = getDefaultUsername(email);
        }

        User user = new User();
        user.setUsername(usernameInput);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.Titiper.toString());
        user.setStatus(Status.Aktif.toString());
        user.setEmail(email);
        userRepository.save(user);

        return user;
    }

    public String getDefaultUsername(String email) {
        return email.split("@")[0];
    }
}

