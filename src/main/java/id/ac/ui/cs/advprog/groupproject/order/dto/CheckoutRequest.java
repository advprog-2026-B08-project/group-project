package id.ac.ui.cs.advprog.groupproject.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotNull
    private UUID buyerId;

    @NotNull
    private UUID productId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotBlank
    private String shippingAddress;
}
