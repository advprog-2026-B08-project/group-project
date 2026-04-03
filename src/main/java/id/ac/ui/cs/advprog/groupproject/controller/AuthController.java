package id.ac.ui.cs.advprog.groupproject.controller;

import id.ac.ui.cs.advprog.groupproject.model.Role;
import id.ac.ui.cs.advprog.groupproject.model.Status;
import id.ac.ui.cs.advprog.groupproject.model.User;
import id.ac.ui.cs.advprog.groupproject.service.CustomUserDetailService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final CustomUserDetailService service;

    public AuthController(CustomUserDetailService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam String email) {

        if (service.emailExists(email)) return "redirect:/register?userExists";
        if (service.confirmPassword(password, confirmPassword)) return "redirect:/register?error";

        service.createUser(email, password, username);
        return "redirect:/login?registered";
    }
}