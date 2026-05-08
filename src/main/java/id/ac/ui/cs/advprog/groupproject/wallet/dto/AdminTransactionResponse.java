package id.ac.ui.cs.advprog.groupproject.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminTransactionResponse {
    private UUID id;
    private UUID walletId;
    private UUID userId;
    private String type;
    private BigDecimal amount;
    private String status;
    private String description;
    private UUID referenceId;
    private LocalDateTime createdAt;
}
