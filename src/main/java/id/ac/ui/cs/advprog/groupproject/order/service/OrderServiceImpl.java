package id.ac.ui.cs.advprog.groupproject.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.groupproject.order.dto.CheckoutRequest;
import id.ac.ui.cs.advprog.groupproject.order.dto.ProductSnapshot;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import id.ac.ui.cs.advprog.groupproject.order.model.StatusHistory;
import id.ac.ui.cs.advprog.groupproject.order.port.JastiperMetricsPort;
import id.ac.ui.cs.advprog.groupproject.order.port.PaymentPort;
import id.ac.ui.cs.advprog.groupproject.order.port.ProductRatingPort;
import id.ac.ui.cs.advprog.groupproject.order.port.StockPort;
import id.ac.ui.cs.advprog.groupproject.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.groupproject.order.repository.StatusHistoryRepository;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static final String ORDER_NOT_FOUND_PREFIX = "Order not found: ";

    private final OrderRepository orderRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final StockPort stockPort;
    private final PaymentPort paymentPort;
    private final JastiperMetricsPort jastiperMetricsPort;
    private final ProductRatingPort productRatingPort;

    public OrderServiceImpl(OrderRepository orderRepository,
                            StatusHistoryRepository statusHistoryRepository,
                            StockPort stockPort,
                            PaymentPort paymentPort,
                            JastiperMetricsPort jastiperMetricsPort,
                            ProductRatingPort productRatingPort) {
        this.orderRepository = orderRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.stockPort = stockPort;
        this.paymentPort = paymentPort;
        this.jastiperMetricsPort = jastiperMetricsPort;
        this.productRatingPort = productRatingPort;
    }

    @Override
    @Transactional
    public Order checkout(CheckoutRequest request) {
        validateCheckoutRequest(request);

        LOGGER.info("order_checkout_started quantity={}", request.getQuantity());

        ProductSnapshot productInfo = stockPort.reserveStock(request.getProductId(), request.getQuantity());

        if (request.getBuyerId().equals(productInfo.jastiperId())) {
            stockPort.releaseStock(request.getProductId(), request.getQuantity());
            LOGGER.warn("order_checkout_rejected reason=self_purchase");
            throw new IllegalArgumentException("Jastiper tidak boleh memesan dari jasanya sendiri!");
        }

        BigDecimal totalPrice = productInfo.pricePerItem().multiply(BigDecimal.valueOf(request.getQuantity()));

        try {
            paymentPort.pay(request.getBuyerId(), totalPrice, "Payment for " + productInfo.productName());
        } catch (Exception e) {
            stockPort.releaseStock(request.getProductId(), request.getQuantity());
            LOGGER.warn("order_checkout_payment_failed reason={}", e.getMessage());
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
        jastiperMetricsPort.incrementTriedToSell(productInfo.jastiperId());

        LOGGER.info("order_checkout_completed orderId={} totalPrice={}",
                savedOrder.getId(), totalPrice);

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
        return updateStatusInternal(orderId, newStatus);
    }

    private Order updateStatusInternal(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND_PREFIX + orderId));

        if (!order.getStatus().canTransitionTo(newStatus)) {
            LOGGER.warn("order_status_transition_rejected orderId={} from={} to={} reason=invalid_transition",
                    orderId, order.getStatus(), newStatus);
            throw new IllegalStateException("Cannot transition from " + order.getStatus() + " to " + newStatus);
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        StatusHistory history = new StatusHistory();
        history.setOrder(savedOrder);
        history.setStatus(newStatus);
        statusHistoryRepository.save(history);

        LOGGER.info("order_status_updated orderId={} from={} to={}",
                orderId, previousStatus, newStatus);

        // Increment successfullySold and credit Jastiper's wallet when order is completed
        if (newStatus == OrderStatus.COMPLETED) {
            jastiperMetricsPort.incrementSuccessfullySold(savedOrder.getJastiperId());
            paymentPort.creditSeller(
                    savedOrder.getJastiperId(),
                    savedOrder.getTotalPrice(),
                    "Pendapatan dari pesanan: " + savedOrder.getId()
            );
            LOGGER.info("order_completed orderId={} jastiperId={} amountCredited={}",
                    savedOrder.getId(), savedOrder.getJastiperId(), savedOrder.getTotalPrice());
        }

        return savedOrder;
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID orderId) {
        Order cancelled = updateStatusInternal(orderId, OrderStatus.CANCELLED);
        stockPort.releaseStock(cancelled.getProductId(), cancelled.getQuantity());
        paymentPort.refund(
                cancelled.getBuyerId(),
                cancelled.getTotalPrice(),
                "Refund for cancelled order: " + cancelled.getId(),
                cancelled.getId()
        );
        LOGGER.info("order_cancelled orderId={} refundAmount={}",
                cancelled.getId(), cancelled.getTotalPrice());
        return cancelled;
    }

    @Override
    @Transactional
    public Order submitRating(UUID orderId, Integer ratingProduk, Integer ratingJastiper) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND_PREFIX + orderId));

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("Rating hanya bisa diberikan untuk order yang sudah COMPLETED");
        }

        if (ratingProduk == null && ratingJastiper == null) {
            throw new IllegalArgumentException("Minimal satu rating (produk atau jastiper) harus diisi");
        }

        applyProductRating(order, ratingProduk);
        applyJastiperRating(order, ratingJastiper);

        Order savedOrder = orderRepository.save(order);

        if (ratingProduk != null) {
            productRatingPort.propagateProductRating(savedOrder, ratingProduk);
        }

        return savedOrder;
    }

    private void applyProductRating(Order order, Integer ratingProduk) {
        if (ratingProduk == null) {
            return;
        }
        if (order.getRatingProduk() != null) {
            throw new IllegalStateException("Order ini sudah diberi rating produk");
        }
        if (ratingProduk < 1 || ratingProduk > 5) {
            throw new IllegalArgumentException("Rating produk harus antara 1 sampai 5");
        }
        order.setRatingProduk(ratingProduk);
    }

    private void applyJastiperRating(Order order, Integer ratingJastiper) {
        if (ratingJastiper == null) {
            return;
        }
        if (order.getRatingJastiper() != null) {
            throw new IllegalStateException("Order ini sudah diberi rating jastiper");
        }
        if (ratingJastiper < 1 || ratingJastiper > 5) {
            throw new IllegalArgumentException("Rating jastiper harus antara 1 sampai 5");
        }
        order.setRatingJastiper(ratingJastiper);
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
        if (request.getBuyerId() == null) {
            throw new IllegalArgumentException("Buyer ID is required");
        }
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new IllegalArgumentException("Invalid quantity");
        }
        if (request.getShippingAddress() == null || request.getShippingAddress().isBlank()) {
            throw new IllegalArgumentException("Address required");
        }
    }
}
