package id.ac.ui.cs.advprog.groupproject.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import id.ac.ui.cs.advprog.groupproject.wallet.dto.AdminTransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.enums.TransactionStatus;
import id.ac.ui.cs.advprog.groupproject.wallet.enums.TransactionType;
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
        when(walletRepository.saveAndFlush(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.createWallet(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(walletRepository).saveAndFlush(any(Wallet.class));
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
    void getBalance_WalletNotFound_CreatesWallet() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.saveAndFlush(any(Wallet.class))).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            return arg;
        });

        WalletResponse response = walletService.getBalance(userId);

        assertEquals(userId, response.getUserId());
        assertEquals(BigDecimal.ZERO, response.getBalance());
    }

    // ===================== topUp tests =====================

    @Test
    void topUp_Success() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("100000"));

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
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
        assertEquals("PENDING", response.getStatus());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
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
    void topUp_WalletNotFound_CreatesWalletAndProceeds() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("100000"));

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.saveAndFlush(any(Wallet.class))).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            return arg;
        });
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> {
                    WalletTransaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    return tx;
                });

        TransactionResponse response = walletService.topUp(userId, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("100000"), response.getAmount());
    }

    // ===================== deductBalance tests =====================

    @Test
    void deductBalance_Success() {
        wallet.setBalance(new BigDecimal("200000"));
        BigDecimal amount = new BigDecimal("75000");

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
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
    void deductBalance_NullDescription_UsesDefaultDescription() {
        wallet.setBalance(new BigDecimal("200000"));
        BigDecimal amount = new BigDecimal("75000");

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> {
                    WalletTransaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    return tx;
                });

        TransactionResponse response = walletService.deductBalance(userId, amount, null);

        assertEquals("DEBIT", response.getType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Deduct sebesar " + amount, response.getDescription());
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
        when(walletRepository.saveAndFlush(any(Wallet.class))).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            return arg;
        });

        BigDecimal amount = new BigDecimal("1000");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> walletService.deductBalance(userId, amount, "Checkout order"));
        
        assertTrue(ex.getMessage().contains("Insufficient balance"));
    }

    @Test
    void deductBalance_InsufficientBalance_ThrowsException() {
        wallet.setBalance(new BigDecimal("5000"));
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.deductBalance(userId, new BigDecimal("10000"), "Checkout order"));
    }

    // ===================== creditBalance tests =====================

    @Test
    void creditBalance_Success() {
        wallet.setBalance(new BigDecimal("100000"));
        BigDecimal amount = new BigDecimal("50000");

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> {
                    WalletTransaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    return tx;
                });

        TransactionResponse response = walletService.creditBalance(userId, amount, "Order earnings");

        assertNotNull(response);
        assertEquals("CREDIT", response.getType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(amount, response.getAmount());
        assertEquals("Order earnings", response.getDescription());
        assertEquals(new BigDecimal("150000"), wallet.getBalance());
    }

    @Test
    void creditBalance_NullDescription_UsesDefaultDescription() {
        wallet.setBalance(new BigDecimal("100000"));
        BigDecimal amount = new BigDecimal("50000");

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> {
                    WalletTransaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    return tx;
                });

        TransactionResponse response = walletService.creditBalance(userId, amount, null);

        assertEquals("CREDIT", response.getType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Pendapatan sebesar " + amount, response.getDescription());
    }

    @Test
    void creditBalance_NullAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.creditBalance(userId, null, "Order earnings"));
    }

    @Test
    void creditBalance_ZeroAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.creditBalance(userId, BigDecimal.ZERO, "Order earnings"));
    }

    @Test
    void creditBalance_NegativeAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.creditBalance(userId, new BigDecimal("-1000"), "Order earnings"));
    }

    @Test
    void creditBalance_WalletNotFound_CreatesWallet() {
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.empty());
        when(walletRepository.saveAndFlush(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> {
                    WalletTransaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    return tx;
                });

        BigDecimal amount = new BigDecimal("50000");
        TransactionResponse response = walletService.creditBalance(userId, amount, "Order earnings");

        assertNotNull(response);
        assertEquals("CREDIT", response.getType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(amount, response.getAmount());
    }

    // ===================== withdrawBalance tests =====================

    @Test
    void withdrawBalance_Success() {
        wallet.setBalance(new BigDecimal("200000"));
        BigDecimal amount = new BigDecimal("75000");

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> {
                    WalletTransaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    return tx;
                });

        TransactionResponse response = walletService.withdrawBalance(userId, amount, "BCA-123");

        assertNotNull(response);
        assertEquals("WITHDRAWAL", response.getType());
        assertEquals("PENDING", response.getStatus());
        assertEquals(amount, response.getAmount());
        assertEquals(new BigDecimal("200000"), wallet.getBalance());
    }

    @Test
    void withdrawBalance_NullAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.withdrawBalance(userId, null, "BCA-123"));
    }

    @Test
    void withdrawBalance_InvalidDestination_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.withdrawBalance(userId, new BigDecimal("1000"), ""));
    }

    @Test
    void withdrawBalance_NullDestination_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.withdrawBalance(userId, new BigDecimal("1000"), null));
    }

    @Test
    void withdrawBalance_InsufficientBalance_ThrowsException() {
        wallet.setBalance(new BigDecimal("5000"));
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.withdrawBalance(userId, new BigDecimal("10000"), "BCA-123"));
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
    void refundBalance_NullDescription_UsesDefaultDescription() {
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

        TransactionResponse response = walletService.refundBalance(userId, amount, null, referenceId);

        assertEquals("REFUND", response.getType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Refund sebesar " + amount, response.getDescription());
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
    void refundBalance_WalletNotFound_CreatesWalletAndProceeds() {
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.empty());
        when(walletRepository.saveAndFlush(any(Wallet.class))).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            return arg;
        });
        when(walletTransactionRepository.saveAndFlush(any(WalletTransaction.class)))
                .thenAnswer(invocation -> {
                    WalletTransaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    return tx;
                });

        TransactionResponse response = walletService.refundBalance(userId, new BigDecimal("1000"), "Refund", UUID.randomUUID());

        assertNotNull(response);
        assertEquals(new BigDecimal("1000"), response.getAmount());
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

    // ===================== verifyTransaction tests =====================

    @Test
    void verifyTransaction_NullStatus_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.verifyTransaction(UUID.randomUUID(), null));
    }

    @Test
    void verifyTransaction_PendingStatus_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.verifyTransaction(UUID.randomUUID(), TransactionStatus.PENDING));
    }

    @Test
    void verifyTransaction_TransactionNotFound_ThrowsException() {
        UUID transactionId = UUID.randomUUID();
        when(walletTransactionRepository.findByIdForUpdate(transactionId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> walletService.verifyTransaction(transactionId, TransactionStatus.SUCCESS));
    }

    @Test
    void verifyTransaction_AlreadyProcessed_ThrowsException() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(wallet.getId());
        transaction.setType(TransactionType.TOP_UP);
        transaction.setAmount(new BigDecimal("5000"));
        transaction.setStatus(TransactionStatus.SUCCESS);

        when(walletTransactionRepository.findByIdForUpdate(transaction.getId())).thenReturn(Optional.of(transaction));

        assertThrows(IllegalStateException.class,
                () -> walletService.verifyTransaction(transaction.getId(), TransactionStatus.SUCCESS));
    }

    @Test
    void verifyTransaction_InvalidType_ThrowsException() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(wallet.getId());
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(new BigDecimal("5000"));
        transaction.setStatus(TransactionStatus.PENDING);

        when(walletTransactionRepository.findByIdForUpdate(transaction.getId())).thenReturn(Optional.of(transaction));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.verifyTransaction(transaction.getId(), TransactionStatus.SUCCESS));
    }

    @Test
    void verifyTransaction_WalletNotFound_ThrowsException() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(wallet.getId());
        transaction.setType(TransactionType.TOP_UP);
        transaction.setAmount(new BigDecimal("5000"));
        transaction.setStatus(TransactionStatus.PENDING);

        when(walletTransactionRepository.findByIdForUpdate(transaction.getId())).thenReturn(Optional.of(transaction));
        when(walletRepository.findByIdForUpdate(wallet.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> walletService.verifyTransaction(transaction.getId(), TransactionStatus.SUCCESS));
    }

    @Test
    void verifyTransaction_WithdrawalInsufficientBalance_ThrowsException() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(wallet.getId());
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(new BigDecimal("5000"));
        transaction.setStatus(TransactionStatus.PENDING);

        wallet.setBalance(new BigDecimal("1000"));

        when(walletTransactionRepository.findByIdForUpdate(transaction.getId())).thenReturn(Optional.of(transaction));
        when(walletRepository.findByIdForUpdate(wallet.getId())).thenReturn(Optional.of(wallet));

        assertThrows(IllegalStateException.class,
                () -> walletService.verifyTransaction(transaction.getId(), TransactionStatus.SUCCESS));
    }

    @Test
    void verifyTransaction_TopUpSuccess_AddsBalance() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(wallet.getId());
        transaction.setType(TransactionType.TOP_UP);
        transaction.setAmount(new BigDecimal("5000"));
        transaction.setStatus(TransactionStatus.PENDING);

        wallet.setBalance(new BigDecimal("10000"));

        when(walletTransactionRepository.findByIdForUpdate(transaction.getId())).thenReturn(Optional.of(transaction));
        when(walletRepository.findByIdForUpdate(wallet.getId())).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);

        TransactionResponse response = walletService.verifyTransaction(transaction.getId(), TransactionStatus.SUCCESS);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("15000"), wallet.getBalance());
    }

    @Test
    void verifyTransaction_WithdrawalSuccess_SubtractsBalance() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(wallet.getId());
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(new BigDecimal("3000"));
        transaction.setStatus(TransactionStatus.PENDING);

        wallet.setBalance(new BigDecimal("10000"));

        when(walletTransactionRepository.findByIdForUpdate(transaction.getId())).thenReturn(Optional.of(transaction));
        when(walletRepository.findByIdForUpdate(wallet.getId())).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);

        TransactionResponse response = walletService.verifyTransaction(transaction.getId(), TransactionStatus.SUCCESS);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("7000"), wallet.getBalance());
    }

    @Test
    void verifyTransaction_Failed_DoesNotChangeBalance() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(wallet.getId());
        transaction.setType(TransactionType.TOP_UP);
        transaction.setAmount(new BigDecimal("5000"));
        transaction.setStatus(TransactionStatus.PENDING);

        wallet.setBalance(new BigDecimal("10000"));

        when(walletTransactionRepository.findByIdForUpdate(transaction.getId())).thenReturn(Optional.of(transaction));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);

        TransactionResponse response = walletService.verifyTransaction(transaction.getId(), TransactionStatus.FAILED);

        assertEquals("FAILED", response.getStatus());
        assertEquals(new BigDecimal("10000"), wallet.getBalance());
    }

    // ===================== getTransactionHistory tests =====================

    @Test
    void getTransactionHistory_ReturnsList() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(wallet.getId());
        transaction.setType(TransactionType.TOP_UP);
        transaction.setAmount(new BigDecimal("10000"));
        transaction.setStatus(TransactionStatus.SUCCESS);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId()))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> responses = walletService.getTransactionHistory(userId);

        assertEquals(1, responses.size());
        assertEquals("TOP_UP", responses.get(0).getType());
    }

    @Test
    void getTransactionHistory_WalletNotFound_CreatesWalletAndReturnsEmpty() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.saveAndFlush(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet w = invocation.getArgument(0);
            w.setId(UUID.randomUUID());
            return w;
        });
        when(walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(any(UUID.class)))
                .thenReturn(List.of());

        List<TransactionResponse> history = walletService.getTransactionHistory(userId);

        assertTrue(history.isEmpty());
    }

    // ===================== getAllTransactions tests =====================

    @Test
    void getAllTransactions_ReturnsMappedResponses() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(wallet.getId());
        transaction.setType(TransactionType.TOP_UP);
        transaction.setAmount(new BigDecimal("10000"));
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription("Top-up success");
        transaction.setReferenceId(UUID.randomUUID());

        Wallet mappedWallet = new Wallet();
        mappedWallet.setId(wallet.getId());
        mappedWallet.setUserId(userId);

        when(walletTransactionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(transaction));
        when(walletRepository.findAllById(any())).thenReturn(List.of(mappedWallet));

        List<AdminTransactionResponse> responses = walletService.getAllTransactions();

        assertEquals(1, responses.size());
        AdminTransactionResponse response = responses.get(0);
        assertEquals(userId, response.getUserId());
        assertEquals("TOP_UP", response.getType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(transaction.getReferenceId(), response.getReferenceId());
    }

    @Test
    void getAllTransactions_MissingWallet_ThrowsException() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(UUID.randomUUID());
        transaction.setType(TransactionType.TOP_UP);
        transaction.setAmount(new BigDecimal("10000"));
        transaction.setStatus(TransactionStatus.SUCCESS);

        when(walletTransactionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(transaction));
        when(walletRepository.findAllById(any())).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> walletService.getAllTransactions());
    }
}
