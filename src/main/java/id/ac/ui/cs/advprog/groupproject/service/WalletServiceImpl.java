package id.ac.ui.cs.advprog.groupproject.service;

import id.ac.ui.cs.advprog.groupproject.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.enums.TransactionStatus;
import id.ac.ui.cs.advprog.groupproject.enums.TransactionType;
import id.ac.ui.cs.advprog.groupproject.model.Wallet;
import id.ac.ui.cs.advprog.groupproject.model.WalletTransaction;
import id.ac.ui.cs.advprog.groupproject.repository.WalletRepository;
import id.ac.ui.cs.advprog.groupproject.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletServiceImpl(WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Override
    public Wallet createWallet(UUID userId) {
        // Cek apakah wallet sudah ada untuk user ini
        if (walletRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("Wallet already exists for user: " + userId);
        }

        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        return walletRepository.save(wallet);
    }

    @Override
    public WalletResponse getBalance(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        return new WalletResponse(
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getUpdatedAt());
    }

    @Override
    @Transactional
    public TransactionResponse topUp(UUID userId, TopUpRequest request) {
        // 1. Validasi amount
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be greater than zero");
        }

        // 2. Cari wallet
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        // 3. Tambah saldo
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        // 4. Catat transaksi
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setType(TransactionType.TOP_UP);
        transaction.setAmount(request.getAmount());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription("Top-up sebesar " + request.getAmount());
        walletTransactionRepository.save(transaction);

        // 5. Return response
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getCreatedAt());
    }
}
