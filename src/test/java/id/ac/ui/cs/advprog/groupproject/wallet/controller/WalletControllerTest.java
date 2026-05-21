package id.ac.ui.cs.advprog.groupproject.wallet.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.WithdrawalRequest;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    private UUID userId;
    private User principal;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        principal = new User();
        principal.setId(userId);
        principal.setRole("ROLE_TITIPER");
    }

    @Test
    void getBalance_ReturnsOk() {
        WalletResponse walletResponse = new WalletResponse(
                userId, new BigDecimal("250000"), LocalDateTime.now());

        when(walletService.getBalance(userId)).thenReturn(walletResponse);

        ResponseEntity<WalletResponse> response = walletController.getBalance(userId, principal);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUserId());
        assertEquals(new BigDecimal("250000"), response.getBody().getBalance());
    }

    @Test
    void getBalance_WalletNotFound_ThrowsException() {
        when(walletService.getBalance(userId))
                .thenThrow(new IllegalArgumentException("Wallet not found"));

        assertThrows(IllegalArgumentException.class, () -> walletController.getBalance(userId, principal));
    }

    @Test
    void getBalance_UnauthorizedUser_ThrowsForbiddenException() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole("ROLE_TITIPER");

        assertThrows(ResponseStatusException.class, () -> walletController.getBalance(userId, otherUser));
    }

    @Test
    void getBalance_AdminUser_ReturnsOk() {
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setRole("ROLE_ADMIN");

        WalletResponse walletResponse = new WalletResponse(
                userId, new BigDecimal("250000"), LocalDateTime.now());
        when(walletService.getBalance(userId)).thenReturn(walletResponse);

        ResponseEntity<WalletResponse> response = walletController.getBalance(userId, adminUser);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void topUp_ReturnsOk() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("100000"));

        TransactionResponse txResponse = new TransactionResponse(
                UUID.randomUUID(), "TOP_UP", new BigDecimal("100000"),
            "PENDING", "Top-up pending sebesar 100000", LocalDateTime.now());

        when(walletService.topUp(eq(userId), any(TopUpRequest.class))).thenReturn(txResponse);

        ResponseEntity<TransactionResponse> response = walletController.topUp(userId, request, principal);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("TOP_UP", response.getBody().getType());
        assertEquals(new BigDecimal("100000"), response.getBody().getAmount());
        assertEquals("PENDING", response.getBody().getStatus());
    }

    @Test
    void topUp_InvalidAmount_ThrowsException() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("-5000"));

        when(walletService.topUp(eq(userId), any(TopUpRequest.class)))
                .thenThrow(new IllegalArgumentException("Top-up amount must be greater than zero"));

        assertThrows(IllegalArgumentException.class, () -> walletController.topUp(userId, request, principal));
    }

    @Test
    void withdraw_ReturnsOk() {
        WithdrawalRequest request = new WithdrawalRequest();
        request.setAmount(new BigDecimal("50000"));
        request.setDestination("BCA-123");

        TransactionResponse txResponse = new TransactionResponse(
                UUID.randomUUID(), "WITHDRAWAL", new BigDecimal("50000"),
                "PENDING", "Withdrawal pending ke BCA-123", LocalDateTime.now());

        when(walletService.withdrawBalance(eq(userId), any(BigDecimal.class), any(String.class)))
                .thenReturn(txResponse);

        ResponseEntity<TransactionResponse> response = walletController.withdraw(userId, request, principal);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("WITHDRAWAL", response.getBody().getType());
        assertEquals(new BigDecimal("50000"), response.getBody().getAmount());
        assertEquals("PENDING", response.getBody().getStatus());
    }

    @Test
    void getTransactionHistory_ReturnsOk() {
        TransactionResponse txResponse = new TransactionResponse(
                UUID.randomUUID(), "REFUND", new BigDecimal("10000"),
                "SUCCESS", "Refund test", LocalDateTime.now());

        when(walletService.getTransactionHistory(userId)).thenReturn(List.of(txResponse));

        ResponseEntity<List<TransactionResponse>> response = walletController.getTransactionHistory(userId, principal);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("REFUND", response.getBody().get(0).getType());
    }
}
