package id.ac.ui.cs.advprog.groupproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class WalletResponse {
    private UUID userId;
    private BigDecimal balance;
    private LocalDateTime updatedAt;
}
