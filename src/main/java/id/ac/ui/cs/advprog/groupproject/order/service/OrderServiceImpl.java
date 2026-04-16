package id.ac.ui.cs.advprog.groupproject.order.service;

import id.ac.ui.cs.advprog.groupproject.order.dto.CheckoutRequest;
import id.ac.ui.cs.advprog.groupproject.order.dto.ProductSnapshot;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import id.ac.ui.cs.advprog.groupproject.order.model.StatusHistory;
import id.ac.ui.cs.advprog.groupproject.order.port.PaymentPort;
import id.ac.ui.cs.advprog.groupproject.order.port.StockPort;
import id.ac.ui.cs.advprog.groupproject.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.groupproject.order.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final StockPort stockPort;
    private final PaymentPort paymentPort;

    public OrderServiceImpl(OrderRepository orderRepository,
                            StatusHistoryRepository statusHistoryRepository,
                            StockPort stockPort,
                            PaymentPort paymentPort) {
        this.orderRepository = orderRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.stockPort = stockPort;
        this.paymentPort = paymentPort;
    }

    @Override
    @Transactional
    public Order checkout(CheckoutRequest request) {
        // 1. Validate inputs
        validateCheckoutRequest(request);

        // 2. Reserve stock (decoupled via StockPort)
        ProductSnapshot productInfo = stockPort.reserveStock(request.getProductId(), request.getQuantity());

        // 3. Prevent self-purchase
        if (request.getBuyerId().equals(productInfo.jastiperId())) {
            stockPort.releaseStock(request.getProductId(), request.getQuantity());
            throw new IllegalArgumentException("Jastiper tidak boleh memesan dari jasanya sendiri!");
        }

        // 4. Calculate total price
        BigDecimal totalPrice = productInfo.pricePerItem().multiply(BigDecimal.valueOf(request.getQuantity()));

        // 5. Pay (decoupled via PaymentPort)
        try {
            paymentPort.pay(request.getBuyerId(), totalPrice, "Payment for " + productInfo.productName());
        } catch (Exception e) {
            stockPort.releaseStock(request.getProductId(), request.getQuantity());
            throw new IllegalArgumentException("Payment failed: " + e.getMessage());
        }

        // 6. Create Order
        Order order = new Order();
        order.setBuyerId(request.getBuyerId());
        order.setJastiperId(productInfo.jastiperId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setShippingAddress(request.getShippingAddress());
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PAID);

        Order savedOrder = orderRepository.save(order);

        // 7. Initial Status History
        StatusHistory history = new StatusHistory();
        history.setOrder(savedOrder);
        history.setStatus(OrderStatus.PAID);
        statusHistoryRepository.save(history);

        return savedOrder;
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> findByBuyerId(UUID buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }

    @Override
    public List<Order> findByJastiperId(UUID jastiperId) {
        return orderRepository.findByJastiperId(jastiperId);
    }

    @Override
    @Transactional
    public Order updateStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new IllegalStateException("Cannot transition from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        StatusHistory history = new StatusHistory();
        history.setOrder(savedOrder);
        history.setStatus(newStatus);
        statusHistoryRepository.save(history);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID orderId) {
        Order order = updateStatus(orderId, OrderStatus.CANCELLED);
        stockPort.releaseStock(order.getProductId(), order.getQuantity());
        // Potential refund logic here via PaymentPort
        return order;
    }

    private void validateCheckoutRequest(CheckoutRequest request) {
        if (request.getBuyerId() == null) throw new IllegalArgumentException("Buyer ID is required");
        if (request.getProductId() == null) throw new IllegalArgumentException("Product ID is required");
        if (request.getQuantity() == null || request.getQuantity() < 1) throw new IllegalArgumentException("Invalid quantity");
        if (request.getShippingAddress() == null || request.getShippingAddress().isBlank()) throw new IllegalArgumentException("Address required");
    }
}
