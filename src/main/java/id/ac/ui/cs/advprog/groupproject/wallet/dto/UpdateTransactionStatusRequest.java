package id.ac.ui.cs.advprog.groupproject.wallet.dto;

import id.ac.ui.cs.advprog.groupproject.wallet.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTransactionStatusRequest {
    @NotNull(message = "Status is required")
    private TransactionStatus status;
}
