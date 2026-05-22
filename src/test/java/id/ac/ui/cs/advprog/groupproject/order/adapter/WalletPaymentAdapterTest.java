package id.ac.ui.cs.advprog.groupproject.order.adapter;

import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
class WalletPaymentAdapterTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletPaymentAdapter walletPaymentAdapter;

    @Test
    void pay_DelegatesToWalletService() {
        UUID userId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("10000");
        String description = "Payment for order";

        walletPaymentAdapter.pay(userId, amount, description);

        verify(walletService).deductBalance(userId, amount, description);
    }

    @Test
    void refund_DelegatesToWalletService() {
        UUID userId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("5000");
        String description = "Refund for order";
        UUID referenceId = UUID.randomUUID();

        walletPaymentAdapter.refund(userId, amount, description, referenceId);

        verify(walletService).refundBalance(userId, amount, description, referenceId);
    }

    @Test
    void creditSeller_DelegatesToWalletService() {
        UUID sellerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("15000");
        String description = "Pendapatan dari pesanan";

        walletPaymentAdapter.creditSeller(sellerId, amount, description);

        verify(walletService).creditBalance(sellerId, amount, description);
    }
}
