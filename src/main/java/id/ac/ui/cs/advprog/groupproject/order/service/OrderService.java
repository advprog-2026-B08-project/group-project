package id.ac.ui.cs.advprog.groupproject.order.service;

import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    public Order createOrder(Order request) {
        return request; // Skeleton: belum ada logika simpan DB
    }
    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        return new Order(); // Skeleton: belum ada logika update DB
    }
}