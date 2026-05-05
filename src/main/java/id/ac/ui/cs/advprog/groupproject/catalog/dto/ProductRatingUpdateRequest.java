package id.ac.ui.cs.advprog.groupproject.catalog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRatingUpdateRequest {
  @NotNull(message = "orderId is required")
  private UUID orderId;

  @NotNull(message = "buyerId is required")
  private UUID buyerId;

  @NotNull(message = "productRating is required")
  @Min(value = 1, message = "productRating must be at least 1")
  @Max(value = 5, message = "productRating must be at most 5")
  private Integer productRating;
}
