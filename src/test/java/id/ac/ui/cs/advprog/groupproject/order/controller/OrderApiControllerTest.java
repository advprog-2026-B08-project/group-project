package id.ac.ui.cs.advprog.groupproject.order.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.groupproject.order.dto.CheckoutRequest;
import id.ac.ui.cs.advprog.groupproject.order.dto.RatingRequest;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import id.ac.ui.cs.advprog.groupproject.order.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class OrderApiControllerTest {

  @Mock
  private OrderService orderService;

  @InjectMocks
  private OrderApiController controller;

  private UUID buyerId;
  private UUID productId;
  private UUID orderId;
  private UUID jastiperId;
  private Order sampleOrder;

  @BeforeEach
  void setUp() {
    buyerId = UUID.randomUUID();
    productId = UUID.randomUUID();
    orderId = UUID.randomUUID();
    jastiperId = UUID.randomUUID();
    sampleOrder = new Order();
    sampleOrder.setId(orderId);
    sampleOrder.setBuyerId(buyerId);
    sampleOrder.setProductId(productId);
    sampleOrder.setQuantity(2);
    sampleOrder.setShippingAddress("Jl. Test No. 1");
    sampleOrder.setTotalPrice(BigDecimal.valueOf(200000));
    sampleOrder.setStatus(OrderStatus.PAID);
  }

  @Test
  void checkout_success_returns201WithOrder() {
    CheckoutRequest request = new CheckoutRequest(buyerId, productId, 2, "Jl. Test No. 1");
    when(orderService.checkout(any(CheckoutRequest.class))).thenReturn(sampleOrder);

    ResponseEntity<?> response = controller.checkout(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertInstanceOf(Order.class, response.getBody());
    Order body = (Order) response.getBody();
    assertEquals(OrderStatus.PAID, body.getStatus());
  }

  @Test
  void checkout_validationFails_returns400WithMessage() {
    when(orderService.checkout(any()))
        .thenThrow(new IllegalArgumentException("Saldo tidak cukup"));
    CheckoutRequest request = new CheckoutRequest(buyerId, productId, 1, "Jl. Test");

    ResponseEntity<?> response = controller.checkout(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Saldo tidak cukup", response.getBody());
  }

  @Test
  void checkout_stockInsufficient_returns400() {
    when(orderService.checkout(any()))
        .thenThrow(new IllegalArgumentException("Insufficient stock"));
    CheckoutRequest request = new CheckoutRequest(buyerId, productId, 99, "Jl. Test");

    ResponseEntity<?> response = controller.checkout(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void checkout_illegalState_returns409() {
    when(orderService.checkout(any()))
        .thenThrow(new IllegalStateException("Conflict state"));
    CheckoutRequest request = new CheckoutRequest(buyerId, productId, 1, "Jl. Test");

    ResponseEntity<?> response = controller.checkout(request);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  void getAllOrders_returns200WithList() {
    when(orderService.findAll()).thenReturn(List.of(sampleOrder));

    ResponseEntity<List<Order>> response = controller.getAllOrders();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getOrderById_found_returns200() {
    when(orderService.findById(orderId)).thenReturn(Optional.of(sampleOrder));

    ResponseEntity<Order> response = controller.getOrderById(orderId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(orderId, response.getBody().getId());
  }

  @Test
  void getOrderById_notFound_returns404() {
    when(orderService.findById(orderId)).thenReturn(Optional.empty());

    ResponseEntity<Order> response = controller.getOrderById(orderId);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void updateStatus_validTransition_returns200() {
    sampleOrder.setStatus(OrderStatus.PURCHASED);
    when(orderService.updateStatus(orderId, OrderStatus.PURCHASED)).thenReturn(sampleOrder);

    ResponseEntity<?> response = controller.updateStatus(orderId, OrderStatus.PURCHASED);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void updateStatus_invalidTransition_returns409() {
    when(orderService.updateStatus(orderId, OrderStatus.COMPLETED))
        .thenThrow(new IllegalStateException("Cannot transition from PAID to COMPLETED"));

    ResponseEntity<?> response = controller.updateStatus(orderId, OrderStatus.COMPLETED);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  void updateStatus_orderNotFound_returns400() {
    when(orderService.updateStatus(orderId, OrderStatus.PURCHASED))
        .thenThrow(new IllegalArgumentException("Order not found"));

    ResponseEntity<?> response = controller.updateStatus(orderId, OrderStatus.PURCHASED);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void cancelOrder_success_returns200() {
    when(orderService.cancelOrder(orderId)).thenReturn(sampleOrder);

    ResponseEntity<?> response = controller.cancelOrder(orderId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(orderService).cancelOrder(orderId);
  }

  @Test
  void cancelOrder_notFound_returns400() {
    when(orderService.cancelOrder(orderId))
        .thenThrow(new IllegalArgumentException("Order not found"));

    ResponseEntity<?> response = controller.cancelOrder(orderId);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void cancelOrder_illegalState_returns409() {
    when(orderService.cancelOrder(orderId))
        .thenThrow(new IllegalStateException("Cannot cancel completed order"));

    ResponseEntity<?> response = controller.cancelOrder(orderId);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  void submitRating_success_returns200() {
    sampleOrder.setStatus(OrderStatus.COMPLETED);
    sampleOrder.setRatingProduk(4);
    when(orderService.submitRating(orderId, 4)).thenReturn(sampleOrder);

    RatingRequest request = new RatingRequest(4);
    ResponseEntity<?> response = controller.submitRating(orderId, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    Order body = (Order) response.getBody();
    assertEquals(4, body.getRatingProduk());
  }

  @Test
  void submitRating_invalidRating_returns400() {
    when(orderService.submitRating(orderId, 0))
        .thenThrow(new IllegalArgumentException("Rating harus antara 1 sampai 5"));

    RatingRequest request = new RatingRequest(0);
    ResponseEntity<?> response = controller.submitRating(orderId, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void submitRating_alreadyRated_returns409() {
    when(orderService.submitRating(orderId, 3))
        .thenThrow(new IllegalStateException("Order ini sudah diberi rating"));

    RatingRequest request = new RatingRequest(3);
    ResponseEntity<?> response = controller.submitRating(orderId, request);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  void getBuyerActiveOrders_returns200WithList() {
    when(orderService.findBuyerActiveOrders(buyerId)).thenReturn(List.of(sampleOrder));

    ResponseEntity<List<Order>> response = controller.getBuyerActiveOrders(buyerId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getBuyerCompletedOrders_returns200WithList() {
    when(orderService.findBuyerCompletedOrders(buyerId)).thenReturn(List.of());

    ResponseEntity<List<Order>> response = controller.getBuyerCompletedOrders(buyerId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getJastiperTodoOrders_returns200WithList() {
    when(orderService.findJastiperTodoOrders(jastiperId)).thenReturn(List.of(sampleOrder));

    ResponseEntity<List<Order>> response = controller.getJastiperTodoOrders(jastiperId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getJastiperDoneOrders_returns200WithList() {
    when(orderService.findJastiperCompletedOrders(jastiperId)).thenReturn(List.of(sampleOrder));

    ResponseEntity<List<Order>> response = controller.getJastiperDoneOrders(jastiperId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }
}
