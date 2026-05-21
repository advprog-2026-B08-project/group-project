package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.service.KycRequestService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PageControllerTest {
    @Mock
    private KycRequestService kycRequestService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private PageController pageController;

    @Test
    public void testLogin() {
        String view = pageController.login();
        assertEquals("auth/login", view);
    }

    @Test
    void testHomepageUnauthorized() {
        Model model = new ExtendedModelMap();
        when(session.getAttribute("unauthorized")).thenReturn(true);

        String view = pageController.homepage(session, model);

        assertEquals("homepage", view);
        assertTrue(model.containsAttribute("unauthorized"));
        verify(session).removeAttribute("unauthorized");
    }

    @Test
    public void testHomepageAuthorized() {
        Model model = new ExtendedModelMap();
        when(session.getAttribute("unauthorized")).thenReturn(null);

        String view = pageController.homepage(session, model);

        assertEquals("homepage", view);
        assertFalse(model.containsAttribute("unauthorized"));
        verify(session, never()).removeAttribute("unauthorized");
    }

    @Test
    public void testRegister() {
        String view = pageController.register();
        assertEquals("auth/register", view);
    }

    @Test
    public void testOrder() {
        String view = pageController.order();
        assertEquals("order/list", view);
    }

    @Test
    public void kycRequestJastiper() {
        String view = pageController.kycRequestJastiper();
        assertEquals("auth/kyc/kyc-jastiper", view);
    }
}
