package id.ac.ui.cs.advprog.groupproject.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;
import id.ac.ui.cs.advprog.groupproject.auth.service.CustomUserDetailService;

@Controller
public class AdminController {
    private final CustomUserDetailService userDetailService;

    public AdminController(CustomUserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @PostMapping("/admin/demote")
    public String demote(@RequestParam UUID userId) {
        userDetailService.demote(userId);
        return "redirect:/admin/userList";
    }

    @PostMapping("/admin/ban")
    public String ban(@RequestParam UUID userId) {
        userDetailService.ban(userId);
        return "redirect:/admin/userList";
    }
}
