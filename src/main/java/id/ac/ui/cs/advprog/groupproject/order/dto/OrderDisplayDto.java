package id.ac.ui.cs.advprog.groupproject.order.dto;

import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDisplayDto {
    private UUID id;
    private UUID buyerId;
    private UUID jastiperId;
    private UUID productId;
    private Integer quantity;
    private String shippingAddress;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer ratingProduk;

    // Enriched fields
    private String productName;
    private String productImageUrl;
    private String buyerUsername;
    private String jastiperUsername;

    public static OrderDisplayDto from(Order order) {
        OrderDisplayDto dto = new OrderDisplayDto();
        dto.setId(order.getId());
        dto.setBuyerId(order.getBuyerId());
        dto.setJastiperId(order.getJastiperId());
        dto.setProductId(order.getProductId());
        dto.setQuantity(order.getQuantity());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setRatingProduk(order.getRatingProduk());
        return dto;
    }
}
