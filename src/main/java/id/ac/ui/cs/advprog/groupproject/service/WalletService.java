package id.ac.ui.cs.advprog.groupproject.service;

import id.ac.ui.cs.advprog.groupproject.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.model.Wallet;

import java.util.UUID;

public interface WalletService {
    Wallet createWallet(UUID userId);

    WalletResponse getBalance(UUID userId);

    TransactionResponse topUp(UUID userId, TopUpRequest request);
}
