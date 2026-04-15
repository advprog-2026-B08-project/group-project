package id.ac.ui.cs.advprog.groupproject.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID buyerId;
    private UUID jastiperId;
    private UUID productId;

    private Integer quantity;
    private String shippingAddress;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (buyerId != null && buyerId.equals(jastiperId)) {
            throw new IllegalArgumentException("Jastiper tidak boleh memesan dari jasanya sendiri!");
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}