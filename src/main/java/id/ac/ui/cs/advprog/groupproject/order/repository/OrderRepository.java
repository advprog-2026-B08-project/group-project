package id.ac.ui.cs.advprog.groupproject.order.repository;

import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
}