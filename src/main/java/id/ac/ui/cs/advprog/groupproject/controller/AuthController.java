package id.ac.ui.cs.advprog.groupproject.controller;


import id.ac.ui.cs.advprog.groupproject.model.User;
import id.ac.ui.cs.advprog.groupproject.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword) {

        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/register?userExists";
        }

        if (!password.equals(confirmPassword)) {
            return "redirect:/register?error";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        //TODO: Nanti tolong diganti ke opsi user, jangan default jastiper
        user.setRole("Jastiper");

        userRepository.save(user);

        return "redirect:/login?registered";
    }
}