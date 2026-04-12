package id.ac.ui.cs.advprog.groupproject.model;

import id.ac.ui.cs.advprog.groupproject.enums.TransactionStatus;
import id.ac.ui.cs.advprog.groupproject.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
@Getter @Setter
@NoArgsConstructor
public class WalletTransaction{
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

}