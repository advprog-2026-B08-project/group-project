package id.ac.ui.cs.advprog.groupproject.controller;

import id.ac.ui.cs.advprog.groupproject.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void getBalance_ReturnsOk() {
        WalletResponse walletResponse = new WalletResponse(
                userId, new BigDecimal("250000"), LocalDateTime.now());

        when(walletService.getBalance(userId)).thenReturn(walletResponse);

        ResponseEntity<WalletResponse> response = walletController.getBalance(userId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUserId());
        assertEquals(new BigDecimal("250000"), response.getBody().getBalance());
    }

    @Test
    void getBalance_WalletNotFound_ThrowsException() {
        when(walletService.getBalance(userId))
                .thenThrow(new IllegalArgumentException("Wallet not found"));

        assertThrows(IllegalArgumentException.class, () -> walletController.getBalance(userId));
    }

    @Test
    void topUp_ReturnsOk() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("100000"));

        TransactionResponse txResponse = new TransactionResponse(
                UUID.randomUUID(), "TOP_UP", new BigDecimal("100000"),
                "SUCCESS", "Top-up sebesar 100000", LocalDateTime.now());

        when(walletService.topUp(eq(userId), any(TopUpRequest.class))).thenReturn(txResponse);

        ResponseEntity<TransactionResponse> response = walletController.topUp(userId, request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("TOP_UP", response.getBody().getType());
        assertEquals(new BigDecimal("100000"), response.getBody().getAmount());
        assertEquals("SUCCESS", response.getBody().getStatus());
    }

    @Test
    void topUp_InvalidAmount_ThrowsException() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("-5000"));

        when(walletService.topUp(eq(userId), any(TopUpRequest.class)))
                .thenThrow(new IllegalArgumentException("Top-up amount must be greater than zero"));

        assertThrows(IllegalArgumentException.class, () -> walletController.topUp(userId, request));
    }
}
