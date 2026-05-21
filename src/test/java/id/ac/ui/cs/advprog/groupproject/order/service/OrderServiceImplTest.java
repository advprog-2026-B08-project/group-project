package id.ac.ui.cs.advprog.groupproject.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.groupproject.order.dto.CheckoutRequest;
import id.ac.ui.cs.advprog.groupproject.order.dto.ProductSnapshot;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import id.ac.ui.cs.advprog.groupproject.order.model.StatusHistory;
import id.ac.ui.cs.advprog.groupproject.order.port.PaymentPort;
import id.ac.ui.cs.advprog.groupproject.order.port.StockPort;
import id.ac.ui.cs.advprog.groupproject.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.groupproject.order.repository.StatusHistoryRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.service.CatalogService;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private StatusHistoryRepository statusHistoryRepository;

  @Mock
  private StockPort stockPort;

  @Mock
  private PaymentPort paymentPort;

  @Mock
  private CatalogService catalogService;

  @Mock
  private UserRepository userRepository;

  private OrderServiceImpl orderService;
  private UUID buyerId;
  private UUID jastiperId;
  private UUID productId;
  private ProductSnapshot productSnapshot;

  @BeforeEach
  void setUp() {
    orderService = new OrderServiceImpl(
        orderRepository, statusHistoryRepository, stockPort, paymentPort,
        catalogService, userRepository);
    buyerId = UUID.randomUUID();
    jastiperId = UUID.randomUUID();
    productId = UUID.randomUUID();
    productSnapshot = new ProductSnapshot(
        jastiperId, BigDecimal.valueOf(100000), "Produk Test");
  }

  @Test
  void checkout_success_returnsOrderWithStatusPaid() {
    when(stockPort.reserveStock(productId, 2)).thenReturn(productSnapshot);
    Order savedOrder = new Order();
    savedOrder.setId(UUID.randomUUID());
    savedOrder.setStatus(OrderStatus.PAID);
    when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
    when(statusHistoryRepository.save(any(StatusHistory.class)))
        .thenReturn(new StatusHistory());
    CheckoutRequest request = new CheckoutRequest(buyerId, productId, 2, "Jl. Merdeka No. 1");

    Order result = orderService.checkout(request);

    assertEquals(OrderStatus.PAID, result.getStatus());
    verify(stockPort).reserveStock(productId, 2);
    verify(paymentPort).pay(eq(buyerId), eq(BigDecimal.valueOf(200000)), anyString());
    verify(orderRepository).save(any(Order.class));
    verify(statusHistoryRepository).save(any(StatusHistory.class));
  }

  @Test
  void checkout_insufficientStock_throwsAndDoesNotPay() {
    when(stockPort.reserveStock(productId, 5))
        .thenThrow(new IllegalArgumentException("Insufficient stock"));
    CheckoutRequest request = new CheckoutRequest(buyerId, productId, 5, "Jl. Merdeka No. 1");

    assertThrows(IllegalArgumentException.class, () -> orderService.checkout(request));
    verify(paymentPort, never()).pay(any(), any(), any());
    verify(orderRepository, never()).save(any());
  }

  @Test
  void checkout_paymentFails_releasesStockAndThrows() {
    when(stockPort.reserveStock(productId, 1)).thenReturn(productSnapshot);
    doThrow(new IllegalArgumentException("Saldo tidak cukup"))
        .when(paymentPort).pay(any(), any(), any());
    CheckoutRequest request = new CheckoutRequest(buyerId, productId, 1, "Jl. Merdeka No. 1");

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class, () -> orderService.checkout(request));

    assertTrue(ex.getMessage().contains("Payment failed"));
    verify(stockPort).releaseStock(productId, 1);
    verify(orderRepository, never()).save(any());
  }

  @Test
  void checkout_selfPurchase_releasesStockAndThrows() {
    UUID sameId = UUID.randomUUID();
    ProductSnapshot selfSnapshot = new ProductSnapshot(
        sameId, BigDecimal.valueOf(50000), "Self Product");
    when(stockPort.reserveStock(productId, 1)).thenReturn(selfSnapshot);
    CheckoutRequest request = new CheckoutRequest(sameId, productId, 1, "Jl. Test No. 1");

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class, () -> orderService.checkout(request));

    assertTrue(ex.getMessage().contains("sendiri"));
    verify(stockPort).releaseStock(productId, 1);
    verify(paymentPort, never()).pay(any(), any(), any());
  }

  @Test
  void checkout_nullBuyerId_throws() {
    CheckoutRequest request = new CheckoutRequest(null, productId, 1, "Jl. Test");
    assertThrows(IllegalArgumentException.class, () -> orderService.checkout(request));
  }

  @Test
  void checkout_nullProductId_throws() {
    CheckoutRequest request = new CheckoutRequest(buyerId, null, 1, "Jl. Test");
    assertThrows(IllegalArgumentException.class, () -> orderService.checkout(request));
  }

  @Test
  void checkout_zeroQuantity_throws() {
    CheckoutRequest request = new CheckoutRequest(buyerId, productId, 0, "Jl. Test");
    assertThrows(IllegalArgumentException.class, () -> orderService.checkout(request));
  }

  @Test
  void checkout_blankAddress_throws() {
    CheckoutRequest request = new CheckoutRequest(buyerId, productId, 1, "   ");
    assertThrows(IllegalArgumentException.class, () -> orderService.checkout(request));
  }

  @Test
  void findAll_returnsAllOrdersFromRepository() {
    List<Order> orders = List.of(new Order(), new Order());
    when(orderRepository.findAll()).thenReturn(orders);

    List<Order> result = orderService.findAll();

    assertEquals(2, result.size());
    verify(orderRepository).findAll();
  }

  @Test
  void findById_found_returnsOrder() {
    UUID id = UUID.randomUUID();
    Order order = new Order();
    order.setId(id);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));

    Optional<Order> result = orderService.findById(id);

    assertTrue(result.isPresent());
    assertEquals(id, result.get().getId());
  }

  @Test
  void findById_notFound_returnsEmpty() {
    UUID id = UUID.randomUUID();
    when(orderRepository.findById(id)).thenReturn(Optional.empty());

    Optional<Order> result = orderService.findById(id);

    assertFalse(result.isPresent());
  }

  @Test
  void findByBuyerId_returnsBuyerOrders() {
    when(orderRepository.findByBuyerId(buyerId)).thenReturn(List.of(new Order()));
    assertEquals(1, orderService.findByBuyerId(buyerId).size());
  }

  @Test
  void findByJastiperId_returnsJastiperOrders() {
    when(orderRepository.findByJastiperId(jastiperId))
        .thenReturn(List.of(new Order(), new Order()));
    assertEquals(2, orderService.findByJastiperId(jastiperId).size());
  }

  @Test
  void updateStatus_validTransition_paidToPurchased() {
    UUID orderId = UUID.randomUUID();
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.PAID);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(statusHistoryRepository.save(any(StatusHistory.class)))
        .thenReturn(new StatusHistory());

    orderService.updateStatus(orderId, OrderStatus.PURCHASED);

    verify(orderRepository).save(order);
    verify(statusHistoryRepository).save(any(StatusHistory.class));
    assertEquals(OrderStatus.PURCHASED, order.getStatus());
  }

  @Test
  void updateStatus_transitionToCompleted_creditsJastiper() {
    UUID orderId = UUID.randomUUID();
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.SHIPPED);
    order.setJastiperId(jastiperId);
    order.setTotalPrice(BigDecimal.valueOf(150000));
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(statusHistoryRepository.save(any(StatusHistory.class)))
        .thenReturn(new StatusHistory());

    orderService.updateStatus(orderId, OrderStatus.COMPLETED);

    verify(orderRepository).save(order);
    verify(statusHistoryRepository).save(any(StatusHistory.class));
    verify(paymentPort).creditSeller(eq(jastiperId), eq(BigDecimal.valueOf(150000)), anyString());
    assertEquals(OrderStatus.COMPLETED, order.getStatus());
  }

  @Test
  void updateStatus_invalidTransition_throws() {
    UUID orderId = UUID.randomUUID();
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.PAID);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    assertThrows(IllegalStateException.class,
        () -> orderService.updateStatus(orderId, OrderStatus.COMPLETED));
    verify(orderRepository, never()).save(any());
  }

  @Test
  void updateStatus_orderNotFound_throws() {
    UUID orderId = UUID.randomUUID();
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> orderService.updateStatus(orderId, OrderStatus.PURCHASED));
  }

  @Test
  void cancelOrder_releasesStockAndSetsStatusCancelled() {
    UUID orderId = UUID.randomUUID();
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.PAID);
    order.setProductId(productId);
    order.setQuantity(3);
    order.setBuyerId(buyerId);
    order.setTotalPrice(BigDecimal.valueOf(300000));
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(statusHistoryRepository.save(any(StatusHistory.class)))
        .thenReturn(new StatusHistory());

    orderService.cancelOrder(orderId);

    verify(stockPort).releaseStock(productId, 3);
    verify(paymentPort).refund(eq(buyerId), eq(BigDecimal.valueOf(300000)), anyString(), eq(orderId));
  }

  @Test
  void submitRating_success_setsRatingAndPropagates() {
    UUID orderId = UUID.randomUUID();
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.COMPLETED);
    order.setBuyerId(buyerId);
    order.setProductId(productId);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);

    id.ac.ui.cs.advprog.groupproject.auth.model.User buyer =
        new id.ac.ui.cs.advprog.groupproject.auth.model.User();
    buyer.setId(buyerId);
    buyer.setUsername("testbuyer");
    when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));

    Order result = orderService.submitRating(orderId, 4);

    assertEquals(4, result.getRatingProduk());
    verify(orderRepository).save(order);
    verify(catalogService).applyProductRating(eq(productId), any(), eq(buyer));
  }

  @Test
  void submitRating_notCompleted_throws() {
    UUID orderId = UUID.randomUUID();
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.PAID);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    assertThrows(IllegalStateException.class, () -> orderService.submitRating(orderId, 4));
    verify(orderRepository, never()).save(any());
  }

  @Test
  void submitRating_alreadyRated_throws() {
    UUID orderId = UUID.randomUUID();
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.COMPLETED);
    order.setRatingProduk(3);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    assertThrows(IllegalStateException.class, () -> orderService.submitRating(orderId, 4));
    verify(orderRepository, never()).save(any());
  }

  @Test
  void submitRating_invalidRating_throws() {
    UUID orderId = UUID.randomUUID();
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.COMPLETED);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    assertThrows(IllegalArgumentException.class, () -> orderService.submitRating(orderId, 0));
    assertThrows(IllegalArgumentException.class, () -> orderService.submitRating(orderId, 6));
    verify(orderRepository, never()).save(any());
  }

  @Test
  void submitRating_orderNotFound_throws() {
    UUID orderId = UUID.randomUUID();
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> orderService.submitRating(orderId, 4));
  }

  @Test
  void submitRating_catalogPropagationFails_stillSavesRating() {
    UUID orderId = UUID.randomUUID();
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(OrderStatus.COMPLETED);
    order.setBuyerId(buyerId);
    order.setProductId(productId);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);

    id.ac.ui.cs.advprog.groupproject.auth.model.User buyer =
        new id.ac.ui.cs.advprog.groupproject.auth.model.User();
    buyer.setId(buyerId);
    when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
    doThrow(new RuntimeException("Catalog down")).when(catalogService)
        .applyProductRating(any(), any(), any());

    Order result = orderService.submitRating(orderId, 5);

    assertEquals(5, result.getRatingProduk());
    verify(orderRepository).save(order);
  }

  @Test
  void findBuyerActiveOrders_returnsCorrectOrders() {
    List<Order> expected = List.of(new Order());
    when(orderRepository.findByBuyerIdAndStatusIn(buyerId,
            List.of(OrderStatus.PAID, OrderStatus.PURCHASED, OrderStatus.SHIPPED)))
            .thenReturn(expected);

    List<Order> result = orderService.findBuyerActiveOrders(buyerId);
    assertEquals(expected, result);
  }

  @Test
  void findBuyerCompletedOrders_returnsCorrectOrders() {
    List<Order> expected = List.of(new Order());
    when(orderRepository.findByBuyerIdAndStatusIn(buyerId,
            List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED)))
            .thenReturn(expected);

    List<Order> result = orderService.findBuyerCompletedOrders(buyerId);
    assertEquals(expected, result);
  }

  @Test
  void findJastiperTodoOrders_returnsCorrectOrders() {
    List<Order> expected = List.of(new Order());
    when(orderRepository.findByJastiperIdAndStatusIn(jastiperId,
            List.of(OrderStatus.PAID, OrderStatus.PURCHASED)))
            .thenReturn(expected);

    List<Order> result = orderService.findJastiperTodoOrders(jastiperId);
    assertEquals(expected, result);
  }

  @Test
  void findJastiperCompletedOrders_returnsCorrectOrders() {
    List<Order> expected = List.of(new Order());
    when(orderRepository.findByJastiperIdAndStatusIn(jastiperId,
            List.of(OrderStatus.SHIPPED, OrderStatus.COMPLETED, OrderStatus.CANCELLED)))
            .thenReturn(expected);

    List<Order> result = orderService.findJastiperCompletedOrders(jastiperId);
    assertEquals(expected, result);
  }
}
