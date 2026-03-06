package id.ac.ui.cs.advprog.groupproject.order.service;

import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import id.ac.ui.cs.advprog.groupproject.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order request) {
        return orderRepository.save(request);
    }

    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(java.util.UUID.fromString(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order tidak ditemukan"));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}