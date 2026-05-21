package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.service.CustomUserDetailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private CustomUserDetailService userDetailService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testRegisterEmailExists() {
        when(userDetailService.emailExists("test@gmail.com")).thenReturn(true);

        String view = authController.register("a", "a", "a", "test@gmail.com", "a a ron");

        assertEquals("redirect:/register?userExists", view);
        verify(userDetailService, never()).createUser(any(), any(), any(), any());
    }

    @Test
    void testRegisterPasswordNotMatching() {
        when(userDetailService.emailExists("test@gmail.com")).thenReturn(false);
        when(userDetailService.confirmPassword("a", "b")).thenReturn(false);

        String view = authController.register("a", "a", "b", "test@gmail.com", "a a ron");

        assertEquals("redirect:/register?error", view);
        verify(userDetailService, never()).createUser(any(), any(), any(), any());
    }

    @Test
    public void testRegisterSuccess() {
        when(userDetailService.emailExists("test@gmail.com")).thenReturn(false);
        when(userDetailService.confirmPassword("a", "a")).thenReturn(true);

        String view = authController.register("a", "a", "a", "test@gmail.com", "a a ron");

        assertEquals("redirect:/login?registered", view);
        verify(userDetailService).createUser("test@gmail.com", "a", "a", "a a ron");
    }
}
