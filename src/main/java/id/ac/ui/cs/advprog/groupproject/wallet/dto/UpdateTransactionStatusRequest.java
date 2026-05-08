package id.ac.ui.cs.advprog.groupproject.wallet.dto;

import id.ac.ui.cs.advprog.groupproject.wallet.enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTransactionStatusRequest {
    private TransactionStatus status;
}
