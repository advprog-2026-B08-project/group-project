package id.ac.ui.cs.advprog.groupproject.auth.controller;

import jakarta.servlet.http.HttpSession;
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
        if (session.getAttribute("unauthorized") != null) {
            model.addAttribute("unauthorized", true);
            session.removeAttribute("unauthorized");
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
        return "auth/admin/admin";
    }

    @GetMapping("/order")
    public String order() {
        return "order/list";
    }

    @GetMapping("/kycRequestJastiper")
    public String kycRequestJastiper() {
        return "auth/kyc/kyc-jastiper";
    }

    @GetMapping("/kycRequestAdmin")
    public String kycRequestAdmin() {
        return "auth/kyc/kyc-admin";
    }

    @GetMapping("/admin/userList")
    public String userList() {
        return "auth/admin/userList";
    }

    @GetMapping("/admin/kycRequestList")
    public String kycRequestList() {
        return "auth/admin/approve-kyc";
    }
}
