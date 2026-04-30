package id.ac.ui.cs.advprog.groupproject.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import id.ac.ui.cs.advprog.groupproject.wallet.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.model.Wallet;

public interface WalletService {
    Wallet createWallet(UUID userId);

    WalletResponse getBalance(UUID userId);

    TransactionResponse topUp(UUID userId, TopUpRequest request);

    TransactionResponse deductBalance(UUID userId, BigDecimal amount, String description);

    TransactionResponse refundBalance(UUID userId, BigDecimal amount, String description, UUID referenceId);
}
