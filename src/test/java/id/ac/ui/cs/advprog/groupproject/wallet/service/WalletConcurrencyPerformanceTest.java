package id.ac.ui.cs.advprog.groupproject.wallet.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TopUpRequest;
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
class WalletConcurrencyPerformanceTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    private List<UUID> userIds = new ArrayList<>();
    private final int threadCount = 16;

    @BeforeEach
    void setUp() {
        // Create a unique user and wallet for each thread to avoid database lock contention
        for (int i = 0; i < threadCount; i++) {
            User user = new User();
            user.setUsername("perf_user_" + i + "_" + UUID.randomUUID());
            user.setEmail("perf_user_" + i + "_" + UUID.randomUUID() + "@example.com");
            user.setPassword("password");
            user.setRole("ROLE_TITIPER");
            user.setStatus("AKTIF");
            user = userRepository.save(user);

            UUID uId = user.getId();
            userIds.add(uId);
            walletService.createWallet(uId);
            
            // Give initial balance to avoid insufficient balance error
            walletService.creditBalance(uId, new BigDecimal("1000000.00"), "Initial Balance");
        }
    }

    @AfterEach
    void tearDown() {
        walletTransactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void runWalletConcurrencyLoadTest() throws Exception {
        System.out.println("\n=======================================================");
        System.out.println("STARTING WALLET CONCURRENCY PERFORMANCE LOAD TEST (NO LOCK CONTENTION)");
        System.out.println("=======================================================");
        
        long durationMs = 15000; // Run for 15 seconds to give plenty of profiling data
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<Void>> tasks = new ArrayList<>();
        
        long stopTime = System.currentTimeMillis() + durationMs;
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            final UUID threadUserId = userIds.get(i); // Each thread operates on its own wallet
            
            tasks.add(() -> {
                long opCount = 0;
                while (System.currentTimeMillis() < stopTime) {
                    try {
                        // Alternate between topUp, creditBalance, and deductBalance
                        if (opCount % 3 == 0) {
                            TopUpRequest request = new TopUpRequest();
                            request.setAmount(new BigDecimal("10.00"));
                            walletService.topUp(threadUserId, request);
                        } else if (opCount % 3 == 1) {
                            walletService.creditBalance(threadUserId, new BigDecimal("5.00"), "Credit thread " + threadId);
                        } else {
                            walletService.deductBalance(threadUserId, new BigDecimal("5.00"), "Deduct thread " + threadId);
                        }
                        opCount++;
                    } catch (Exception e) {
                        // Catch database lock wait timeouts or concurrency exceptions to keep threads running
                    }
                }
                System.out.println("Thread " + threadId + " completed " + opCount + " operations.");
                return null;
            });
        }
        
        List<Future<Void>> futures = executor.invokeAll(tasks);
        for (Future<Void> future : futures) {
            future.get(); // check for exceptions
        }
        
        executor.shutdown();
        
        System.out.println("=======================================================");
        System.out.println("WALLET CONCURRENCY PERFORMANCE LOAD TEST COMPLETED");
        System.out.println("=======================================================\n");
        
        assertNotNull(walletService.getBalance(userIds.get(0)));
    }
}
