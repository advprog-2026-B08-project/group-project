package id.ac.ui.cs.advprog.groupproject.service;

import id.ac.ui.cs.advprog.groupproject.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.model.Wallet;
import id.ac.ui.cs.advprog.groupproject.model.WalletTransaction;
import id.ac.ui.cs.advprog.groupproject.repository.WalletRepository;
import id.ac.ui.cs.advprog.groupproject.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
}
