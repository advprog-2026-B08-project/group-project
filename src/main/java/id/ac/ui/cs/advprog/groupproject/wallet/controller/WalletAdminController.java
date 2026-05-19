package id.ac.ui.cs.advprog.groupproject.wallet.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import id.ac.ui.cs.advprog.groupproject.wallet.dto.AdminTransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.UpdateTransactionStatusRequest;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/wallet")
public class WalletAdminController {

    private final WalletService walletService;

    public WalletAdminController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<AdminTransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(walletService.getAllTransactions());
    }

    @PatchMapping("/transactions/{id}/status")
    public ResponseEntity<TransactionResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionStatusRequest request) {
        if (request == null || request.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        TransactionResponse response = walletService.verifyTransaction(id, request.getStatus());
        return ResponseEntity.ok(response);
    }
}
