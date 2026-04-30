package id.ac.ui.cs.advprog.groupproject.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.groupproject.wallet.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.enums.TransactionStatus;
import id.ac.ui.cs.advprog.groupproject.wallet.enums.TransactionType;
import id.ac.ui.cs.advprog.groupproject.wallet.model.Wallet;
import id.ac.ui.cs.advprog.groupproject.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.groupproject.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.groupproject.wallet.repository.WalletTransactionRepository;

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

    @Override
    @Transactional
    public TransactionResponse deductBalance(UUID userId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deduct amount must be greater than zero");
        }

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for user: " + userId);
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        if (description == null || description.isBlank()) {
            transaction.setDescription("Deduct sebesar " + amount);
        } else {
            transaction.setDescription(description);
        }
        walletTransactionRepository.save(transaction);

        return new TransactionResponse(
                transaction.getId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getCreatedAt());
    }

    @Override
    @Transactional
    public TransactionResponse refundBalance(UUID userId, BigDecimal amount, String description, UUID referenceId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than zero");
        }
        if (referenceId == null) {
            throw new IllegalArgumentException("Reference ID is required for refund");
        }

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setReferenceId(referenceId);
        transaction.setType(TransactionType.REFUND);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        if (description == null || description.isBlank()) {
            transaction.setDescription("Refund sebesar " + amount);
        } else {
            transaction.setDescription(description);
        }

        try {
            walletTransactionRepository.save(transaction);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Refund already processed for reference: " + referenceId, ex);
        }

        return new TransactionResponse(
                transaction.getId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getCreatedAt());
    }
}
