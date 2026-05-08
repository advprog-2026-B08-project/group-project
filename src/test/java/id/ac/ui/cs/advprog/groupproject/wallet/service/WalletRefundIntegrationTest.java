package id.ac.ui.cs.advprog.groupproject.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.enums.TransactionStatus;
import id.ac.ui.cs.advprog.groupproject.wallet.enums.TransactionType;
import id.ac.ui.cs.advprog.groupproject.wallet.model.Wallet;
import id.ac.ui.cs.advprog.groupproject.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.groupproject.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.groupproject.wallet.repository.WalletTransactionRepository;

@SpringBootTest(properties = {
    "cloudinary.cloud-name=test-cloud",
    "cloudinary.api-key=test-key",
    "cloudinary.api-secret=test-secret",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
@ActiveProfiles("test")
class WalletRefundIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("user_" + UUID.randomUUID());
        user.setEmail("user_" + UUID.randomUUID() + "@example.com");
        user.setPassword("password");
        user.setRole("ROLE_TITIPER");
        user.setStatus("AKTIF");
        user = userRepository.save(user);

        userId = user.getId();
        wallet = walletService.createWallet(userId);
    }

    @AfterEach
    void tearDown() {
        walletTransactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void refundBalance_PersistsTransaction() {
        BigDecimal amount = new BigDecimal("10000");
        UUID referenceId = UUID.randomUUID();

        TransactionResponse response =
                walletService.refundBalance(userId, amount, "Refund test", referenceId);

        assertNotNull(response.getId());
        assertEquals("REFUND", response.getType());
        assertEquals(0, amount.compareTo(response.getAmount()));

        Wallet updated = walletRepository.findByUserId(userId).orElseThrow();
        assertEquals(0, amount.compareTo(updated.getBalance()));

        List<WalletTransaction> transactions =
                walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
        assertEquals(1, transactions.size());
        WalletTransaction tx = transactions.get(0);
        assertEquals(referenceId, tx.getReferenceId());
        assertEquals(TransactionType.REFUND, tx.getType());
        assertEquals(TransactionStatus.SUCCESS, tx.getStatus());
        assertEquals(0, amount.compareTo(tx.getAmount()));
    }

    @Test
    void refundBalance_DuplicateReference_ThrowsException() {
        BigDecimal amount = new BigDecimal("10000");
        UUID referenceId = UUID.randomUUID();

        walletService.refundBalance(userId, amount, "Refund test", referenceId);

        assertThrows(IllegalStateException.class, () ->
                walletService.refundBalance(userId, amount, "Refund test", referenceId));
    }
}
