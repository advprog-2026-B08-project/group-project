package id.ac.ui.cs.advprog.groupproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private String type;
    private BigDecimal amount;
    private String status;
    private String description;
    private LocalDateTime createdAt;

}
