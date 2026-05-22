package id.ac.ui.cs.advprog.groupproject.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequest {

    @Min(1)
    @Max(5)
    private Integer ratingProduk;

    @Min(1)
    @Max(5)
    private Integer ratingJastiper;
}
