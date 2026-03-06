package id.ac.ui.cs.advprog.groupproject.controller;

import id.ac.ui.cs.advprog.groupproject.dto.TopUpRequest;
import id.ac.ui.cs.advprog.groupproject.dto.TransactionResponse;
import id.ac.ui.cs.advprog.groupproject.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
            @RequestBody TopUpRequest request) {
        TransactionResponse response = walletService.topUp(userId, request);
        return ResponseEntity.ok(response);
    }
}
