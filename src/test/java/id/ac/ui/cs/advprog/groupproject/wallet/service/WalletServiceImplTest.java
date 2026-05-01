package id.ac.ui.cs.advprog.groupproject.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import id.ac.ui.cs.advprog.groupproject.wallet.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.model.Wallet;
import id.ac.ui.cs.advprog.groupproject.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.groupproject.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.groupproject.wallet.repository.WalletTransactionRepository;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private UUID userId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
    }

    // ===================== createWallet tests =====================

    @Test
    void createWallet_Success() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.createWallet(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_AlreadyExists_ThrowsException() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalStateException.class, () -> walletService.createWallet(userId));
        verify(walletRepository, never()).save(any());
    }

    // ===================== getBalance tests =====================

    @Test
    void getBalance_Success() {
        wallet.setBalance(new BigDecimal("150000"));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.getBalance(userId);

        assertEquals(userId, response.getUserId());
        assertEquals(new BigDecimal("150000"), response.getBalance());
    }

    @Test
    void getBalance_WalletNotFound_ThrowsException() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> walletService.getBalance(userId));
    }

    // ===================== topUp tests =====================

    @Test
    void topUp_Success() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("100000"));

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> {
                    WalletTransaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    return tx;
                });

        TransactionResponse response = walletService.topUp(userId, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("100000"), response.getAmount());
        assertEquals("TOP_UP", response.getType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("100000"), wallet.getBalance());
    }

    @Test
    void topUp_NullAmount_ThrowsException() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(null);

        assertThrows(IllegalArgumentException.class, () -> walletService.topUp(userId, request));
    }

    @Test
    void topUp_ZeroAmount_ThrowsException() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> walletService.topUp(userId, request));
    }

    @Test
    void topUp_NegativeAmount_ThrowsException() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("-50000"));

        assertThrows(IllegalArgumentException.class, () -> walletService.topUp(userId, request));
    }

    @Test
    void topUp_WalletNotFound_ThrowsException() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("100000"));

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> walletService.topUp(userId, request));
    }

    // ===================== deductBalance tests =====================

    @Test
    void deductBalance_Success() {
        wallet.setBalance(new BigDecimal("200000"));
        BigDecimal amount = new BigDecimal("75000");

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> {
                    WalletTransaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    return tx;
                });

        TransactionResponse response = walletService.deductBalance(userId, amount, "Checkout order");

        assertNotNull(response);
        assertEquals("DEBIT", response.getType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(amount, response.getAmount());
        assertEquals("Checkout order", response.getDescription());
        assertEquals(new BigDecimal("125000"), wallet.getBalance());
    }

    @Test
    void deductBalance_NullAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.deductBalance(userId, null, "Checkout order"));
    }

    @Test
    void deductBalance_ZeroAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.deductBalance(userId, BigDecimal.ZERO, "Checkout order"));
    }

    @Test
    void deductBalance_NegativeAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.deductBalance(userId, new BigDecimal("-1000"), "Checkout order"));
    }

    @Test
    void deductBalance_WalletNotFound_ThrowsException() {
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> walletService.deductBalance(userId, new BigDecimal("1000"), "Checkout order"));
    }

    @Test
    void deductBalance_InsufficientBalance_ThrowsException() {
        wallet.setBalance(new BigDecimal("5000"));
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.deductBalance(userId, new BigDecimal("10000"), "Checkout order"));
    }

    // ===================== refundBalance tests =====================

    @Test
    void refundBalance_Success() {
        wallet.setBalance(new BigDecimal("10000"));
        BigDecimal amount = new BigDecimal("5000");
        UUID referenceId = UUID.randomUUID();

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletTransactionRepository.saveAndFlush(any(WalletTransaction.class)))
                .thenAnswer(invocation -> {
                    WalletTransaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    return tx;
                });

        TransactionResponse response = walletService.refundBalance(userId, amount, "Refund order", referenceId);

        assertNotNull(response);
        assertEquals("REFUND", response.getType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(amount, response.getAmount());
        assertEquals("Refund order", response.getDescription());
        assertEquals(new BigDecimal("15000"), wallet.getBalance());
    }

    @Test
    void refundBalance_NullAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.refundBalance(userId, null, "Refund", UUID.randomUUID()));
    }

    @Test
    void refundBalance_ZeroAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.refundBalance(userId, BigDecimal.ZERO, "Refund", UUID.randomUUID()));
    }

    @Test
    void refundBalance_NullReferenceId_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.refundBalance(userId, new BigDecimal("1000"), "Refund", null));
    }

    @Test
    void refundBalance_WalletNotFound_ThrowsException() {
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> walletService.refundBalance(userId, new BigDecimal("1000"), "Refund", UUID.randomUUID()));
    }

    @Test
    void refundBalance_DuplicateReference_ThrowsException() {
        wallet.setBalance(new BigDecimal("10000"));
        UUID referenceId = UUID.randomUUID();

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletTransactionRepository.saveAndFlush(any(WalletTransaction.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(IllegalStateException.class,
                () -> walletService.refundBalance(userId, new BigDecimal("1000"), "Refund", referenceId));
    }
}
