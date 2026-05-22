package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.KycRequest;
import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.ActionLogService;
import id.ac.ui.cs.advprog.groupproject.auth.service.CustomUserDetailService;
import id.ac.ui.cs.advprog.groupproject.auth.service.KycRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private CustomUserDetailService userDetailService;

    @Mock
    private KycRequestService requestService;

    @Mock
    private ActionLogService logService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void testAdmin() {
        Model model = new ExtendedModelMap();

        when(userDetailService.getUserCountByRole()).thenReturn(Map.of());
        when(requestService.getRequestCountByStatus()).thenReturn(Map.of());

        String view = adminController.admin(model);

        assertEquals("auth/admin/admin", view);
        assertTrue(model.containsAttribute("roleCount"));
        assertTrue(model.containsAttribute("requestCount"));

        verify(userDetailService).getUserCountByRole();
        verify(requestService).getRequestCountByStatus();
    }

    @Test
    void testUserList() {
        Model model = new ExtendedModelMap();
        User user = new User();
        user.setRole(Role.ROLE_ADMIN.toString());

        List<User> users = new ArrayList<>();
        Page<User> userPage = new PageImpl<>(users);

        when(userDetailService.getFilteredUsers(user, null, 0 ,30)).thenReturn(userPage);

        String view = adminController.userList(0, null, model, user);

        assertEquals("auth/admin/userList", view);
        assertTrue(model.containsAttribute("userList"));
        assertTrue(model.containsAttribute("userPage"));
        assertTrue(model.containsAttribute("currentPage"));
        assertTrue(model.containsAttribute("totalPages"));

        verify(userDetailService).getFilteredUsers(user, null, 0, 30);
    }

    @Test
    void testKycRequestListNoUUID() {
        Model model = new ExtendedModelMap();

        when(requestService.getRequestList()).thenReturn(List.of());
        when(requestService.getRequestCountByStatus()).thenReturn(Map.of());

        String view = adminController.kycRequestList(model, null);

        assertEquals("auth/admin/approve-kyc", view);
        assertTrue(model.containsAttribute("requestList"));
        assertTrue(model.containsAttribute("requestCount"));
        assertFalse(model.containsAttribute("selectedRequest"));

        verify(requestService).getRequestList();
        verify(requestService).getRequestCountByStatus();
        verify(requestService, never()).getById(any());
    }

    @Test
    void testKycRequestListWithUUID() {
        Model model = new ExtendedModelMap();
        UUID uuid = UUID.randomUUID();

        when(requestService.getRequestList()).thenReturn(List.of());
        when(requestService.getRequestCountByStatus()).thenReturn(Map.of());
        when(requestService.getById(uuid)).thenReturn(new KycRequest());

        String view = adminController.kycRequestList(model, uuid);

        assertEquals("auth/admin/approve-kyc", view);
        assertTrue(model.containsAttribute("requestList"));
        assertTrue(model.containsAttribute("requestCount"));
        assertTrue(model.containsAttribute("selectedRequest"));

        verify(requestService).getRequestList();
        verify(requestService).getRequestCountByStatus();
        verify(requestService).getById(uuid);
    }

    @Test
    void testAdminLogs() {
        Model model = new ExtendedModelMap();
        when(logService.getAllLogs()).thenReturn(List.of());

        String view = adminController.adminLogs(model);

        assertEquals("auth/admin/auditLog", view);
        assertTrue(model.containsAttribute("logs"));

        verify(logService).getAllLogs();
    }

    @Test
    void testDemote() {
        User admin = new User();
        UUID id = UUID.randomUUID();

        doNothing().when(userDetailService).demote(admin, id);

        String view = adminController.demote(admin,id);

        assertEquals("redirect:/admin/userList", view);
        verify(userDetailService).demote(admin, id);
    }

    @Test
    void testBan() {
        User admin = new User();
        UUID id = UUID.randomUUID();

        doNothing().when(userDetailService).ban(admin, id);

        String view = adminController.ban(admin, id);

        assertEquals("redirect:/admin/userList", view);
        verify(userDetailService).ban(admin, id);
    }

    @Test
    void testLiftBan() {
        User admin = new User();
        UUID id = UUID.randomUUID();

        doNothing().when(userDetailService).liftBan(admin, id);

        String view = adminController.liftBan(admin, id);

        assertEquals("redirect:/admin/userList", view);
        verify(userDetailService).liftBan(admin, id);
    }

    @Test
    void testAcceptKycRequest() {
        User admin = new User();
        UUID id = UUID.randomUUID();

        doNothing().when(requestService).closeAcceptedRequest(admin, id);

        String view = adminController.acceptRequest(admin, id);

        assertEquals("redirect:/admin/kycRequestList", view);
        verify(requestService).closeAcceptedRequest(admin, id);
    }

    @Test
    void testRejectKycRequest() {
        User admin = new User();
        UUID id = UUID.randomUUID();

        doNothing().when(requestService).closeRejectedRequest(admin, id);

        String view = adminController.rejectRequest(admin, id);

        assertEquals("redirect:/admin/kycRequestList", view);
        verify(requestService).closeRejectedRequest(admin, id);
    }
}
