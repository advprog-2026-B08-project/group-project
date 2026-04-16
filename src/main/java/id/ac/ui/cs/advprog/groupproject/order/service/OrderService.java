package id.ac.ui.cs.advprog.groupproject.order.service;

import id.ac.ui.cs.advprog.groupproject.order.dto.CheckoutRequest;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
    Order checkout(CheckoutRequest request);
    List<Order> findAll();
    Optional<Order> findById(UUID id);
    List<Order> findByBuyerId(UUID buyerId);
    List<Order> findByJastiperId(UUID jastiperId);
    Order updateStatus(UUID orderId, OrderStatus newStatus);
    Order cancelOrder(UUID orderId);
}