package id.ac.ui.cs.advprog.groupproject.wallet.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawalRequest {
    private BigDecimal amount;
    private String destination;
}
