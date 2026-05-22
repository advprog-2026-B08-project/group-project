package id.ac.ui.cs.advprog.groupproject.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_buyer_id", columnList = "buyerId"),
    @Index(name = "idx_order_jastiper_id", columnList = "jastiperId"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_buyer_status", columnList = "buyerId, status"),
    @Index(name = "idx_order_jastiper_status", columnList = "jastiperId, status")
})
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "jastiper_id", nullable = false)
    private UUID jastiperId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    private Integer quantity;
    private String shippingAddress;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Integer ratingJastiper;
    private Integer ratingProduk;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id != null && Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}