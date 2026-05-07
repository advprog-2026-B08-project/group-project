package id.ac.ui.cs.advprog.groupproject.order.repository;

import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByBuyerId(UUID buyerId);
    List<Order> findByJastiperId(UUID jastiperId);
    List<Order> findByBuyerIdAndStatusIn(UUID buyerId, List<OrderStatus> statuses);
    List<Order> findByJastiperIdAndStatusIn(UUID jastiperId, List<OrderStatus> statuses);
}