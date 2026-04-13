package id.ac.ui.cs.advprog.groupproject.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CatalogDto {
    private UUID id;
    private UUID jastiperId;
    private String jastiperUsername;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    private String imageUrl;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotNull(message = "originLocation is required")
    private String originLocation;

    @NotNull(message = "travelDate is required")
    private LocalDate travelDate;
}
