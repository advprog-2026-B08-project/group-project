package id.ac.ui.cs.advprog.groupproject.wallet.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.groupproject.wallet.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.WithdrawalRequest;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<WalletResponse> getBalance(@PathVariable UUID userId) {
        WalletResponse response = walletService.getBalance(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/top-up/{userId}")
    public ResponseEntity<TransactionResponse> topUp(
            @PathVariable UUID userId,
            @Valid @RequestBody TopUpRequest request) {
        TransactionResponse response = walletService.topUp(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw/{userId}")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable UUID userId,
            @Valid @RequestBody WithdrawalRequest request) {
        TransactionResponse response = walletService.withdrawBalance(
                userId,
                request.getAmount(),
                request.getDestination());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(@PathVariable UUID userId) {
        List<TransactionResponse> response = walletService.getTransactionHistory(userId);
        return ResponseEntity.ok(response);
    }
}
