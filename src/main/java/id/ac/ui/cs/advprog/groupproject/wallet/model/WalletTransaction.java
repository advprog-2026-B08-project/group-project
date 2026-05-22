package id.ac.ui.cs.advprog.groupproject.wallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import id.ac.ui.cs.advprog.groupproject.wallet.enums.TransactionStatus;
import id.ac.ui.cs.advprog.groupproject.wallet.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "wallet_transactions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_wallet_transactions_reference",
            columnNames = {"wallet_id", "reference_id", "type"}
        )
    }
)
@Getter @Setter
@NoArgsConstructor
public class WalletTransaction{
    public static class Builder {
        private UUID walletId;
        private UUID referenceId;
        private TransactionType type;
        private BigDecimal amount;
        private String description;
        private TransactionStatus status;

        public Builder walletId(UUID walletId) {
            this.walletId = walletId;
            return this;
        }

        public Builder referenceId(UUID referenceId) {
            this.referenceId = referenceId;
            return this;
        }

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder status(TransactionStatus status) {
            this.status = status;
            return this;
        }

        public WalletTransaction build() {
            WalletTransaction transaction = new WalletTransaction();
            transaction.setWalletId(this.walletId);
            transaction.setReferenceId(this.referenceId);
            transaction.setType(this.type);
            transaction.setAmount(this.amount);
            transaction.setDescription(this.description);
            transaction.setStatus(this.status);
            return transaction;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private UUID walletId;

    @Column(name = "reference_id")
    private UUID referenceId;

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