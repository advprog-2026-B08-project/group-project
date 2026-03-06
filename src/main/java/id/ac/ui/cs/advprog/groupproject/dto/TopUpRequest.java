package id.ac.ui.cs.advprog.groupproject.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class TopUpRequest {
    private BigDecimal amount;
}
