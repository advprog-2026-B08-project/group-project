package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.CustomUserDetailService;
import id.ac.ui.cs.advprog.groupproject.auth.service.KycRequestService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class AdminDataController {
    private final CustomUserDetailService userDetailsService;
    private final KycRequestService requestService;

    public AdminDataController (CustomUserDetailService userDetailsService, KycRequestService requestService) {
        this.userDetailsService = userDetailsService;
        this.requestService = requestService;
    }

    @ModelAttribute("roleCount")
    public Map<String, Long> roleCount() {
        return userDetailsService.getUserCountByRole();
    }

    @ModelAttribute("requestCount")
    public Map<String, Long> requestCount() {
        return requestService.getRequestCountByStatus();
    }

    @ModelAttribute("userList")
    public List<User> getUserList() {
        return userDetailsService.getUserList();
    }

}
