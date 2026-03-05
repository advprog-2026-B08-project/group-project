package id.ac.ui.cs.advprog.groupproject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;
import java.time.LocalDate;

@Entity 
@Getter @Setter
@Table(name = "Catalog")
public class Catalog {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotBlank(message = "Product name is required")
    private String name;
    
    private String description;

    @Column(name = "image_url")
    private String imageUrl;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;
    
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;
    
    @NotNull(message = "originLocation is required")
    @Column(name = "origin_location")
    private String originLocation;

    @NotNull(message = "travelDate is required")
    @Column(name = "travel_date")
    private LocalDate travelDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User jastiper;
}