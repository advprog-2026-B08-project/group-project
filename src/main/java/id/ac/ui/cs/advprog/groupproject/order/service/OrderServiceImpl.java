package id.ac.ui.cs.advprog.groupproject.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.groupproject.order.dto.CheckoutRequest;
import id.ac.ui.cs.advprog.groupproject.order.dto.ProductSnapshot;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import id.ac.ui.cs.advprog.groupproject.order.model.StatusHistory;
import id.ac.ui.cs.advprog.groupproject.order.port.PaymentPort;
import id.ac.ui.cs.advprog.groupproject.order.port.StockPort;
import id.ac.ui.cs.advprog.groupproject.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.groupproject.order.repository.StatusHistoryRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.service.CatalogService;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final StockPort stockPort;
    private final PaymentPort paymentPort;
    private final CatalogService catalogService;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            StatusHistoryRepository statusHistoryRepository,
                            StockPort stockPort,
                            PaymentPort paymentPort,
                            CatalogService catalogService,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.stockPort = stockPort;
        this.paymentPort = paymentPort;
        this.catalogService = catalogService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Order checkout(CheckoutRequest request) {
        validateCheckoutRequest(request);

        ProductSnapshot productInfo = stockPort.reserveStock(request.getProductId(), request.getQuantity());

        if (request.getBuyerId().equals(productInfo.jastiperId())) {
            stockPort.releaseStock(request.getProductId(), request.getQuantity());
            throw new IllegalArgumentException("Jastiper tidak boleh memesan dari jasanya sendiri!");
        }

        BigDecimal totalPrice = productInfo.pricePerItem().multiply(BigDecimal.valueOf(request.getQuantity()));

        try {
            paymentPort.pay(request.getBuyerId(), totalPrice, "Payment for " + productInfo.productName());
        } catch (Exception e) {
            stockPort.releaseStock(request.getProductId(), request.getQuantity());
            throw new IllegalArgumentException("Payment failed: " + e.getMessage());
        }

        Order order = new Order();
        order.setBuyerId(request.getBuyerId());
        order.setJastiperId(productInfo.jastiperId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setShippingAddress(request.getShippingAddress());
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PAID);

        Order savedOrder = orderRepository.save(order);

        StatusHistory history = new StatusHistory();
        history.setOrder(savedOrder);
        history.setStatus(OrderStatus.PAID);
        statusHistoryRepository.save(history);

        // Increment triedToSell for jastiper (new order = jastiper mencoba menjual)
        incrementTriedToSell(productInfo.jastiperId());

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

        // Increment successfullySold when order is completed
        if (newStatus == OrderStatus.COMPLETED) {
            incrementSuccessfullySold(savedOrder.getJastiperId());
        }

        return savedOrder;
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        Order cancelled = updateStatus(orderId, OrderStatus.CANCELLED);
        stockPort.releaseStock(cancelled.getProductId(), cancelled.getQuantity());
        paymentPort.refund(
                cancelled.getBuyerId(),
                cancelled.getTotalPrice(),
            "Refund for cancelled order: " + cancelled.getId(),
            cancelled.getId()
        );
        return cancelled;
    }

    @Override
    @Transactional
    public Order submitRating(UUID orderId, int ratingProduk) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("Rating hanya bisa diberikan untuk order yang sudah COMPLETED");
        }

        if (order.getRatingProduk() != null) {
            throw new IllegalStateException("Order ini sudah diberi rating");
        }

        if (ratingProduk < 1 || ratingProduk > 5) {
            throw new IllegalArgumentException("Rating harus antara 1 sampai 5");
        }

        order.setRatingProduk(ratingProduk);

        Order savedOrder = orderRepository.save(order);

        // Propagate product rating to catalog aggregate
        try {
            User buyer = userRepository.findById(order.getBuyerId())
                    .orElse(null);
            if (buyer != null) {
                ProductRatingUpdateRequest ratingRequest = new ProductRatingUpdateRequest();
                ratingRequest.setOrderId(orderId);
                ratingRequest.setBuyerId(order.getBuyerId());
                ratingRequest.setProductRating(ratingProduk);
                catalogService.applyProductRating(order.getProductId(), ratingRequest, buyer);
            }
        } catch (Exception e) {
            // Log but don't fail the order rating if catalog update fails
            // The order rating is already saved
        }

        return savedOrder;
    }

    // Titiper: active orders (in progress)
    @Override
    public List<Order> findBuyerActiveOrders(UUID buyerId) {
        return orderRepository.findByBuyerIdAndStatusIn(buyerId,
                List.of(OrderStatus.PAID, OrderStatus.PURCHASED, OrderStatus.SHIPPED));
    }

    // Titiper: completed/cancelled orders
    @Override
    public List<Order> findBuyerCompletedOrders(UUID buyerId) {
        return orderRepository.findByBuyerIdAndStatusIn(buyerId,
                List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
    }

    // Jastiper: to-do list (orders that need action)
    @Override
    public List<Order> findJastiperTodoOrders(UUID jastiperId) {
        return orderRepository.findByJastiperIdAndStatusIn(jastiperId,
                List.of(OrderStatus.PAID, OrderStatus.PURCHASED));
    }

    // Jastiper: completed/shipped/cancelled orders
    @Override
    public List<Order> findJastiperCompletedOrders(UUID jastiperId) {
        return orderRepository.findByJastiperIdAndStatusIn(jastiperId,
                List.of(OrderStatus.SHIPPED, OrderStatus.COMPLETED, OrderStatus.CANCELLED));
    }

    private void validateCheckoutRequest(CheckoutRequest request) {
        if (request.getBuyerId() == null) throw new IllegalArgumentException("Buyer ID is required");
        if (request.getProductId() == null) throw new IllegalArgumentException("Product ID is required");
        if (request.getQuantity() == null || request.getQuantity() < 1) throw new IllegalArgumentException("Invalid quantity");
        if (request.getShippingAddress() == null || request.getShippingAddress().isBlank()) throw new IllegalArgumentException("Address required");
    }

    /**
     * Increment triedToSell counter for jastiper when a new order is placed.
     * This affects the jastiper's success rate (successfullySold / triedToSell).
     */
    private void incrementTriedToSell(UUID jastiperId) {
        try {
            userRepository.findById(jastiperId).ifPresent(jastiper -> {
                jastiper.setTriedToSell(jastiper.getTriedToSell() + 1);
                userRepository.save(jastiper);
            });
        } catch (Exception e) {
            // Log but don't fail the checkout if success rate update fails
        }
    }

    /**
     * Increment successfullySold counter for jastiper when an order is completed.
     * This affects the jastiper's success rate (successfullySold / triedToSell).
     */
    private void incrementSuccessfullySold(UUID jastiperId) {
        try {
            userRepository.findById(jastiperId).ifPresent(jastiper -> {
                jastiper.setSuccessfullySold(jastiper.getSuccessfullySold() + 1);
                userRepository.save(jastiper);
            });
        } catch (Exception e) {
            // Log but don't fail the status update if success rate update fails
        }
    }

    /**
     * Propagate product rating to catalog aggregate.
     */
    private void propagateProductRating(Order order, int ratingProduk) {
        try {
            User buyer = userRepository.findById(order.getBuyerId())
                    .orElse(null);
            if (buyer != null) {
                ProductRatingUpdateRequest ratingRequest = new ProductRatingUpdateRequest();
                ratingRequest.setOrderId(order.getId());
                ratingRequest.setBuyerId(order.getBuyerId());
                ratingRequest.setProductRating(ratingProduk);
                catalogService.applyProductRating(order.getProductId(), ratingRequest, buyer);
            }
        } catch (Exception e) {
            // Log but don't fail the order rating if catalog update fails
            // The order rating is already saved
        }
    }
}
