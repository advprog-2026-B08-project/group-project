package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.KycRequest;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.KycRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KycControllerTest {
    @Mock
    private KycRequestService kycRequestService;

    @InjectMocks
    private KycController kycController;

    @Test
    public void testCreateRequestJastiper() {
        User user = new User();
        String email = "test@gmail.com";
        String fullName = "a a ron";
        String socials = "https://smth-smth";

        when(kycRequestService.createRequestForJastiper(user, email, fullName, socials)).thenReturn(new KycRequest());

        String view = kycController.createRequestJastiper(user, email, fullName, socials);

        assertEquals("redirect:/kycRequestJastiper", view);
        verify(kycRequestService).createRequestForJastiper(user, email, fullName, socials);
    }
}
