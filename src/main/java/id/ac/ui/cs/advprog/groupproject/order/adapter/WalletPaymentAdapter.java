package id.ac.ui.cs.advprog.groupproject.order.adapter;

import id.ac.ui.cs.advprog.groupproject.order.port.PaymentPort;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class WalletPaymentAdapter implements PaymentPort {

    private final WalletService walletService;

    public WalletPaymentAdapter(WalletService walletService) {
        this.walletService = walletService;
    }

    @Override
    public void pay(UUID userId, BigDecimal amount, String description) {
        walletService.deductBalance(userId, amount, description);
    }

    @Override
    public void refund(UUID userId, BigDecimal amount, String description) {
        // Wallet group hasn't implemented creditBalance or refund yet,
        // so we use topUp conceptually or wait for their update.
        // For now, this is a placeholder to keep the interface SOLID.
    }
}
