package id.ac.ui.cs.advprog.groupproject.catalog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@Setter
@Table(name = "catalog_rating_event")
public class CatalogRatingEvent {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "order_id", nullable = false, unique = true)
  private UUID orderId;

  @Column(name = "catalog_id", nullable = false)
  private UUID catalogId;

  @Column(name = "buyer_id", nullable = false)
  private UUID buyerId;

  @Column(name = "product_rating", nullable = false)
  private Integer productRating;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
