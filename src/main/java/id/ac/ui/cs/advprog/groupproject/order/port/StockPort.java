package id.ac.ui.cs.advprog.groupproject.order.port;

import id.ac.ui.cs.advprog.groupproject.order.dto.ProductSnapshot;

import java.util.UUID;

public interface StockPort {
    ProductSnapshot reserveStock(UUID productId, int quantity);
    void releaseStock(UUID productId, int quantity);
}
