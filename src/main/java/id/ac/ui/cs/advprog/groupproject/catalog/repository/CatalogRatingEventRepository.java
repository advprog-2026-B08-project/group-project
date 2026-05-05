package id.ac.ui.cs.advprog.groupproject.catalog.repository;

import id.ac.ui.cs.advprog.groupproject.catalog.model.CatalogRatingEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogRatingEventRepository extends JpaRepository<CatalogRatingEvent, UUID> {
  boolean existsByOrderId(UUID orderId);
}
