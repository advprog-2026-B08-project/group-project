package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.CustomUserDetailService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProfileController {
    private final CustomUserDetailService userService;

    public ProfileController(CustomUserDetailService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "auth/profile/profile";
    }

    @GetMapping("/profile/edit")
    public String profileEdit(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "auth/profile/profileEdit";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User user,
                                @RequestParam String username,
                                @RequestParam (required = false) String socials,
                                @RequestParam (required = false) String fullName) {

        userService.updateProfile(user.getId(), username, socials, fullName);

        return "redirect:/profile";
    }
}
