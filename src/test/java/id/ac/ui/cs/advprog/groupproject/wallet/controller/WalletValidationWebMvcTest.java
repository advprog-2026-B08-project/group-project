package id.ac.ui.cs.advprog.groupproject.wallet.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;

@WebMvcTest(controllers = {WalletController.class, WalletAdminController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(WalletExceptionHandler.class)
class WalletValidationWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletService walletService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public WalletService walletService() {
            return Mockito.mock(WalletService.class);
        }
    }

    @Test
    void topUp_InvalidAmount_ReturnsBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        User principal = new User();
        principal.setId(userId);
        principal.setRole("ROLE_TITIPER");
        String payload = "{\"amount\":0}";

        mockMvc.perform(post("/api/wallet/top-up/" + userId)
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Amount must be greater than zero"));

        verifyNoInteractions(walletService);
    }

    @Test
    void withdraw_InvalidDestination_ReturnsBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        User principal = new User();
        principal.setId(userId);
        principal.setRole("ROLE_TITIPER");
        String payload = "{\"amount\":1000,\"destination\":\"\"}";

        mockMvc.perform(post("/api/wallet/withdraw/" + userId)
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Destination is required"));

        verifyNoInteractions(walletService);
    }

    @Test
    void adminUpdateStatus_NullStatus_ReturnsBadRequest() throws Exception {
        UUID transactionId = UUID.randomUUID();
        String payload = "{\"status\":null}";

        mockMvc.perform(patch("/api/admin/wallet/transactions/" + transactionId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Status is required"));

        verifyNoInteractions(walletService);
    }

    @Test
    void getBalance_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/wallet/balance/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid parameter")));

        verifyNoInteractions(walletService);
    }
}
