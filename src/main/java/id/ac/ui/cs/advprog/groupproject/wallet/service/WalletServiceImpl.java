package id.ac.ui.cs.advprog.groupproject.wallet.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletServiceImpl(WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    private Wallet getOrCreateWallet(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    try {
                        Wallet newWallet = new Wallet();
                        newWallet.setUserId(userId);
                        newWallet.setBalance(BigDecimal.ZERO);
                        return walletRepository.saveAndFlush(newWallet);
                    } catch (DataIntegrityViolationException e) {
                        return walletRepository.findByUserId(userId)
                                .orElseThrow(() -> new IllegalStateException("Failed to find or create wallet for user: " + userId, e));
                    }
                });
    }

    private Wallet getOrCreateWalletForUpdate(UUID userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> {
                    try {
                        Wallet newWallet = new Wallet();
                        newWallet.setUserId(userId);
                        newWallet.setBalance(BigDecimal.ZERO);
                        return walletRepository.saveAndFlush(newWallet);
                    } catch (DataIntegrityViolationException e) {
                        return walletRepository.findByUserIdForUpdate(userId)
                                .orElseThrow(() -> new IllegalStateException("Failed to find or create wallet for update for user: " + userId, e));
                    }
                });
    }

    @Override
    @Transactional
    public Wallet createWallet(UUID userId) {
        // Cek apakah wallet sudah ada untuk user ini
        if (walletRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("Wallet already exists for user: " + userId);
        }

        try {
            Wallet wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            return walletRepository.saveAndFlush(wallet);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Wallet already exists for user: " + userId, e);
        }
    }

    @Override
    @Transactional
    public WalletResponse getBalance(UUID userId) {
        Wallet wallet = getOrCreateWallet(userId);

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

        // 2. Cari atau buat wallet
        Wallet wallet = getOrCreateWallet(userId);

        // 3. Catat transaksi pending
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.TOP_UP)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .description("Top-up pending sebesar " + request.getAmount())
                .build();
        walletTransactionRepository.save(transaction);

        // 4. Return response
        return toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse withdrawBalance(UUID userId, BigDecimal amount, String destination) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Withdrawal destination is required");
        }

        Wallet wallet = getOrCreateWalletForUpdate(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for user: " + userId);
        }

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.WITHDRAWAL)
                .amount(amount)
                .status(TransactionStatus.PENDING)
                .description("Withdrawal pending ke " + destination)
                .build();
        walletTransactionRepository.save(transaction);

        return toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse deductBalance(UUID userId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deduct amount must be greater than zero");
        }

        Wallet wallet = getOrCreateWalletForUpdate(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for user: " + userId);
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.DEBIT)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .description(description == null || description.isBlank() ? "Deduct sebesar " + amount : description)
                .build();
        walletTransactionRepository.save(transaction);

        return toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse creditBalance(UUID userId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be greater than zero");
        }

        Wallet wallet = getOrCreateWalletForUpdate(userId);

        wallet.setBalance(wallet.getBalance().add(amount));

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.CREDIT)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .description(description == null || description.isBlank() ? "Pendapatan sebesar " + amount : description)
                .build();
        walletTransactionRepository.save(transaction);

        return toResponse(transaction);
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

        Wallet wallet = getOrCreateWalletForUpdate(userId);

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .referenceId(referenceId)
                .type(TransactionType.REFUND)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .description(description == null || description.isBlank() ? "Refund sebesar " + amount : description)
                .build();

        try {
            walletTransactionRepository.saveAndFlush(transaction);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Refund already processed for reference: " + referenceId, ex);
        }

        return toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse verifyTransaction(UUID transactionId, TransactionStatus status) {
        if (status == null || status == TransactionStatus.PENDING) {
            throw new IllegalArgumentException("Status must be SUCCESS or FAILED");
        }

        WalletTransaction transaction = walletTransactionRepository.findByIdForUpdate(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction already processed: " + transactionId);
        }

        if (transaction.getType() != TransactionType.TOP_UP
                && transaction.getType() != TransactionType.WITHDRAWAL) {
            throw new IllegalArgumentException("Transaction type cannot be verified: " + transaction.getType());
        }

        if (status == TransactionStatus.SUCCESS) {
            Wallet wallet = walletRepository.findByIdForUpdate(transaction.getWalletId())
                    .orElseThrow(() -> new IllegalStateException("Wallet not found for transaction: " + transactionId));

            if (transaction.getType() == TransactionType.TOP_UP) {
                wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
            } else {
                if (wallet.getBalance().compareTo(transaction.getAmount()) < 0) {
                    throw new IllegalStateException("Insufficient balance for withdrawal: " + wallet.getUserId());
                }
                wallet.setBalance(wallet.getBalance().subtract(transaction.getAmount()));
            }

            walletRepository.save(wallet);
        }

        transaction.setStatus(status);
        walletTransactionRepository.save(transaction);
        return toResponse(transaction);
    }

    @Override
    @Transactional
    public List<TransactionResponse> getTransactionHistory(UUID userId) {
        Wallet wallet = getOrCreateWallet(userId);

        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminTransactionResponse> getAllTransactions() {
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByOrderByCreatedAtDesc();
        Set<UUID> walletIds = transactions.stream()
                .map(WalletTransaction::getWalletId)
                .collect(Collectors.toSet());

        Map<UUID, UUID> walletUserMap = walletRepository.findAllById(walletIds)
                .stream()
                .collect(Collectors.toMap(Wallet::getId, Wallet::getUserId));

        return transactions.stream()
                .map(transaction -> {
                    UUID userId = walletUserMap.get(transaction.getWalletId());
                    if (userId == null) {
                        throw new IllegalStateException("Wallet not found for transaction: " + transaction.getId());
                    }
                    return toAdminResponse(transaction, userId);
                })
                .toList();
    }

    private TransactionResponse toResponse(WalletTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getCreatedAt());
    }

    private AdminTransactionResponse toAdminResponse(WalletTransaction transaction, UUID userId) {
        return new AdminTransactionResponse(
                transaction.getId(),
                transaction.getWalletId(),
                userId,
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getReferenceId(),
                transaction.getCreatedAt());
    }
}
