package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.KycRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/homepage")
    public String homepage(HttpSession session, Model model) {
        System.out.println("SESSION unauthorized = " + session.getAttribute("unauthorized"));

        if (session.getAttribute("unauthorized") != null) {
            model.addAttribute("unauthorized", true);
            session.removeAttribute("unauthorized"); // 🔥 important (one-time)
        }
        return "homepage";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/profile")
    public String profile() {
        return "auth/profile";
    }

    @GetMapping("/admin")
    public String admin() {
        return "auth/admin";
    }

    @GetMapping("/order")
    public String order() {
        return "auth/order";
    }

    @GetMapping("/kycRequestJastiper")
    public String kycRequestJastiper() {
        return "auth/kyc/kyc-jastiper";
    }

    @GetMapping("/kycRequestAdmin")
    public String kycRequestAdmin() {
        return "auth/kyc/kyc-admin";
    }
}
