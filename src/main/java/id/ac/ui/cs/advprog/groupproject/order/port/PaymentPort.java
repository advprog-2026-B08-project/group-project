package id.ac.ui.cs.advprog.groupproject.order.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentPort {
    void pay(UUID userId, BigDecimal amount, String description);
    void refund(UUID userId, BigDecimal amount, String description, UUID referenceId);
    void creditSeller(UUID sellerId, BigDecimal amount, String description);
}
