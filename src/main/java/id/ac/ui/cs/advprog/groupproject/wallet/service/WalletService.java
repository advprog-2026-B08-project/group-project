package id.ac.ui.cs.advprog.groupproject.wallet.service;

import id.ac.ui.cs.advprog.groupproject.wallet.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.model.Wallet;

import java.util.UUID;

public interface WalletService {
    Wallet createWallet(UUID userId);

    WalletResponse getBalance(UUID userId);

    TransactionResponse topUp(UUID userId, TopUpRequest request);
}
