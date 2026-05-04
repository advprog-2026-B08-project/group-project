package id.ac.ui.cs.advprog.groupproject.catalog.model;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDate;
import java.util.UUID;

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

  @NotNull(message = "Rating count is required")
  @Min(value = 0, message = "Rating count cannot be negative")
  @Column(name = "rating_count")
  private Integer ratingCount = 0;

  @NotNull(message = "Rating sum is required")
  @Min(value = 0, message = "Rating sum cannot be negative")
  @Column(name = "rating_sum")
  private Integer ratingSum = 0;

  @NotNull(message = "Rating average is required")
  @Min(value = 0, message = "Rating average cannot be negative")
  @Column(name = "rating_average")
  private Double ratingAverage = 0.0;

  @NotNull(message = "originLocation is required")
  @Column(name = "origin_location")
  private String originLocation;

  @NotNull(message = "travelDate is required")
  @Column(name = "travel_date")
  private LocalDate travelDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User jastiper;

  @Version
  private Long version;
}
