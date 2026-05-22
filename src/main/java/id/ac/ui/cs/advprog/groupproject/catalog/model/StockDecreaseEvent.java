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
@Table(name = "stock_decrease_event")
public class StockDecreaseEvent {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "request_id", nullable = false, unique = true)
  private UUID requestId;

  @Column(name = "catalog_id", nullable = false)
  private UUID catalogId;

  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
