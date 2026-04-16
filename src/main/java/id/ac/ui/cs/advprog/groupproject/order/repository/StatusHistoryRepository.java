package id.ac.ui.cs.advprog.groupproject.order.repository;

import id.ac.ui.cs.advprog.groupproject.order.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, UUID> {
    List<StatusHistory> findByOrderIdOrderByTimestampAsc(UUID orderId);
}
