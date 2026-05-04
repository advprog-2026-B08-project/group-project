package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.KycRequest;
import id.ac.ui.cs.advprog.groupproject.auth.service.KycRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class PageController {
    public final KycRequestService requestService;

    public PageController(KycRequestService requestService) {
        this.requestService = requestService;
    }

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
    public String kycRequestList(Model model,
                                 @RequestParam(required = false) UUID selectedId) {

        if (selectedId != null) {
            KycRequest selected = requestService.getById(selectedId);
            model.addAttribute("selectedRequest", selected);
        }
        return "auth/admin/approve-kyc";
    }
}
