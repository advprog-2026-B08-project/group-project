package id.ac.ui.cs.advprog.groupproject.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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

    private Integer quantity;
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (buyerId != null && buyerId.equals(jastiperId)) {
            throw new IllegalArgumentException("Jastiper tidak boleh memesan dari jasanya sendiri!");
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}