package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.KycRequestService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class KycController {
    KycRequestService kycRequestService;

    public KycController(KycRequestService kycRequestService) {
        this.kycRequestService = kycRequestService;
    }

    @PostMapping("/kycRequestJastiper")
    public String createRequestJastiper(@AuthenticationPrincipal User user,
                                        @RequestParam String email,
                                        @RequestParam String fullName,
                                        @RequestParam String socials) {
        kycRequestService.createRequestForJastiper(user, email, fullName, socials);
        return "redirect:/kycRequestJastiper";
    }
}
