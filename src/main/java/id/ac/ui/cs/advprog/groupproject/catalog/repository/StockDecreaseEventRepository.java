package id.ac.ui.cs.advprog.groupproject.catalog.repository;

import id.ac.ui.cs.advprog.groupproject.catalog.model.StockDecreaseEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockDecreaseEventRepository extends JpaRepository<StockDecreaseEvent, UUID> {
  boolean existsByRequestId(UUID requestId);
}
