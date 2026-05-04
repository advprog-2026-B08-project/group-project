package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.KycRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;
import id.ac.ui.cs.advprog.groupproject.auth.service.CustomUserDetailService;

@Controller
public class AdminController {
    private final CustomUserDetailService userDetailService;
    private final KycRequestService requestService;

    public AdminController(CustomUserDetailService userDetailService, KycRequestService requestService) {
        this.userDetailService = userDetailService;
        this.requestService = requestService;
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

    @PostMapping("/admin/kyc/accept")
    public String acceptRequest(@RequestParam UUID requestId) {
        requestService.closeAcceptedRequest(requestId);
        return "redirect:/admin/kycRequestList";
    }

    @PostMapping("/admin/kyc/reject")
    public String rejectRequest(@RequestParam UUID requestId) {
        requestService.closeRejectedRequest(requestId);
        return "redirect:/admin/kycRequestList";
    }
}
