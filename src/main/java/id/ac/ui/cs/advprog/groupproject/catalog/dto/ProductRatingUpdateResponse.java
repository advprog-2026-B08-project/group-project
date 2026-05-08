package id.ac.ui.cs.advprog.groupproject.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductRatingUpdateResponse {
  private boolean applied;
  private Double ratingAverage;
  private Integer ratingCount;
}
