package id.ac.ui.cs.advprog.groupproject.catalog.dto;

import java.time.LocalDate;
import lombok.Getter;

/**
 * Parameter Object carrying the fields needed to update an existing catalog entry.
 * Internal type — not part of the JSON API surface.
 */
@Getter
public class UpdateCatalogRequest {
  private final String name;
  private final String description;
  private final String imageUrl;
  private final Double price;
  private final Integer stock;
  private final String originLocation;
  private final LocalDate travelDate;

  public UpdateCatalogRequest(
      String name,
      String description,
      String imageUrl,
      Double price,
      Integer stock,
      String originLocation,
      LocalDate travelDate) {
    this.name = name;
    this.description = description;
    this.imageUrl = imageUrl;
    this.price = price;
    this.stock = stock;
    this.originLocation = originLocation;
    this.travelDate = travelDate;
  }
}
