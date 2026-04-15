package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.KycRequestService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

public class KycController {
    KycRequestService kycRequestService;

    public KycController(KycRequestService kycRequestService) {
        this.kycRequestService = kycRequestService;
    }

    // TODO : kurang param lain, implement
    public String createRequestAdmin(@AuthenticationPrincipal User user) {
        return "redirect:/homepage";
    }

    // TODO : kurang param lain, implement
    public String createRequestJastiper(@AuthenticationPrincipal User user) {
        return "redirect:/homepage";
    }
}
