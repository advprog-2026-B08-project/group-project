package id.ac.ui.cs.advprog.groupproject.order.adapter;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.groupproject.order.port.PaymentPort;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;

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
    public void refund(UUID userId, BigDecimal amount, String description, UUID referenceId) {
        walletService.refundBalance(userId, amount, description, referenceId);
    }
}
