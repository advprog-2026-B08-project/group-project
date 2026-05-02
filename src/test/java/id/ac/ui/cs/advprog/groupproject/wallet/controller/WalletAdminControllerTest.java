package id.ac.ui.cs.advprog.groupproject.wallet.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.groupproject.wallet.dto.AdminTransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.UpdateTransactionStatusRequest;
import id.ac.ui.cs.advprog.groupproject.wallet.enums.TransactionStatus;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
class WalletAdminControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletAdminController walletAdminController;

    private UUID transactionId;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
    }

    @Test
    void getAllTransactions_ReturnsOk() {
        AdminTransactionResponse response = new AdminTransactionResponse(
                transactionId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TOP_UP",
                new BigDecimal("10000"),
                "PENDING",
                "Top-up pending",
                null,
                LocalDateTime.now());

        when(walletService.getAllTransactions()).thenReturn(List.of(response));

        ResponseEntity<List<AdminTransactionResponse>> result = walletAdminController.getAllTransactions();

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void updateStatus_ReturnsOk() {
        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest();
        request.setStatus(TransactionStatus.SUCCESS);

        TransactionResponse response = new TransactionResponse(
                transactionId,
                "TOP_UP",
                new BigDecimal("10000"),
                "SUCCESS",
                "Top-up success",
                LocalDateTime.now());

        when(walletService.verifyTransaction(eq(transactionId), eq(TransactionStatus.SUCCESS)))
                .thenReturn(response);

        ResponseEntity<TransactionResponse> result = walletAdminController.updateStatus(transactionId, request);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("SUCCESS", result.getBody().getStatus());
    }

    @Test
    void updateStatus_NullRequest_ThrowsBadRequest() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> walletAdminController.updateStatus(transactionId, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void updateStatus_NullStatus_ThrowsBadRequest() {
        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> walletAdminController.updateStatus(transactionId, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
