package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.KycRequest;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.ActionLogService;
import id.ac.ui.cs.advprog.groupproject.auth.service.CustomUserDetailService;
import id.ac.ui.cs.advprog.groupproject.auth.service.KycRequestService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class AdminController {

    private static final String REDIRECT_USER_LIST = "redirect:/admin/userList";
    private static final String REDIRECT_KYC_REQUEST_LIST = "redirect:/admin/kycRequestList";

    private final CustomUserDetailService userDetailService;
    private final KycRequestService requestService;
    private final ActionLogService logService;

    public AdminController(CustomUserDetailService userDetailService,
                           KycRequestService requestService,
                           ActionLogService logService) {
        this.userDetailService = userDetailService;
        this.requestService = requestService;
        this.logService = logService;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("roleCount", userDetailService.getUserCountByRole());
        model.addAttribute("requestCount", requestService.getRequestCountByStatus());
        return "auth/admin/admin";
    }

    @GetMapping("/userList")
    public String userList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String role,
            Model model,
            @AuthenticationPrincipal User user
    ) {
        Page<User> userPage = userDetailService.getFilteredUsers(user, role, page, 30);

        model.addAttribute("userPage", userPage);
        model.addAttribute("userList", userPage.getContent());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());

        return "auth/admin/userList";
    }

    @GetMapping("/admin/kycRequestList")
    public String kycRequestList(Model model,
                                 @RequestParam(required = false) UUID selectedId) {
        model.addAttribute("requestList", requestService.getRequestList());
        model.addAttribute("requestCount", requestService.getRequestCountByStatus());
        if (selectedId != null) {
            KycRequest selected = requestService.getById(selectedId);
            model.addAttribute("selectedRequest", selected);
        }
        return "auth/admin/approve-kyc";
    }

    @GetMapping("/admin/logs")
    public String adminLogs(Model model) {
        model.addAttribute("logs", logService.getAllLogs());
        return "auth/admin/auditLog";
    }

    @PostMapping("/admin/demote")
    public String demote(@AuthenticationPrincipal User admin,
                         @RequestParam UUID userId) {
        userDetailService.demote(admin, userId);
        return REDIRECT_USER_LIST;
    }

    @PostMapping("/admin/ban")
    public String ban(@AuthenticationPrincipal User admin,
                      @RequestParam UUID userId) {
        userDetailService.ban(admin, userId);
        return REDIRECT_USER_LIST;
    }

    @PostMapping("/admin/liftBan")
    public String liftBan(@AuthenticationPrincipal User admin,
                      @RequestParam UUID userId) {
        userDetailService.liftBan(admin, userId);
        return REDIRECT_USER_LIST;
    }

    @PostMapping("/admin/kyc/accept")
    public String acceptRequest(@AuthenticationPrincipal User admin,
                                @RequestParam UUID requestId) {
        requestService.closeAcceptedRequest(admin, requestId);
        return REDIRECT_KYC_REQUEST_LIST;
    }

    @PostMapping("/admin/kyc/reject")
    public String rejectRequest(@AuthenticationPrincipal User admin,
                                @RequestParam UUID requestId) {
        requestService.closeRejectedRequest(admin, requestId);
        return REDIRECT_KYC_REQUEST_LIST;
    }
}
