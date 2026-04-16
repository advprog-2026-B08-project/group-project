package id.ac.ui.cs.advprog.groupproject.order.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderTest {

  @Test
  void prePersist_setsCreatedAt_whenNull() {
    Order order = new Order();
    order.setBuyerId(UUID.randomUUID());
    order.setJastiperId(UUID.randomUUID());

    order.prePersist();

    assertNotNull(order.getCreatedAt());
  }

  @Test
  void prePersist_doesNotOverrideCreatedAt_whenAlreadySet() {
    Order order = new Order();
    order.setBuyerId(UUID.randomUUID());
    order.setJastiperId(UUID.randomUUID());
    LocalDateTime fixed = LocalDateTime.of(2024, 1, 1, 0, 0);
    order.setCreatedAt(fixed);

    order.prePersist();

    assertEquals(fixed, order.getCreatedAt());
  }

  @Test
  void prePersist_throws_whenBuyerEqualsJastiper() {
    UUID same = UUID.randomUUID();
    Order order = new Order();
    order.setBuyerId(same);
    order.setJastiperId(same);

    assertThrows(IllegalArgumentException.class, order::prePersist);
  }

  @Test
  void prePersist_doesNotThrow_whenBuyerDiffersFromJastiper() {
    Order order = new Order();
    order.setBuyerId(UUID.randomUUID());
    order.setJastiperId(UUID.randomUUID());

    assertDoesNotThrow(order::prePersist);
  }

  @Test
  void prePersist_doesNotThrow_whenBuyerIdIsNull() {
    Order order = new Order();
    order.setJastiperId(UUID.randomUUID());

    assertDoesNotThrow(order::prePersist);
  }

  @Test
  void fields_settersAndGetters_work() {
    Order order = new Order();
    UUID id = UUID.randomUUID();
    UUID buyerId = UUID.randomUUID();
    UUID jastiperId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();

    order.setId(id);
    order.setBuyerId(buyerId);
    order.setJastiperId(jastiperId);
    order.setProductId(productId);
    order.setQuantity(3);
    order.setShippingAddress("Jl. Test No. 1");
    order.setTotalPrice(BigDecimal.valueOf(150000));
    order.setStatus(OrderStatus.PAID);

    assertEquals(id, order.getId());
    assertEquals(buyerId, order.getBuyerId());
    assertEquals(jastiperId, order.getJastiperId());
    assertEquals(productId, order.getProductId());
    assertEquals(3, order.getQuantity());
    assertEquals("Jl. Test No. 1", order.getShippingAddress());
    assertEquals(BigDecimal.valueOf(150000), order.getTotalPrice());
    assertEquals(OrderStatus.PAID, order.getStatus());
  }

  @Test
  void orderStatus_canTransitionTo_paidToPurchased() {
    assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.PURCHASED));
  }

  @Test
  void orderStatus_canTransitionTo_purchasedToShipped() {
    assertTrue(OrderStatus.PURCHASED.canTransitionTo(OrderStatus.SHIPPED));
  }

  @Test
  void orderStatus_canTransitionTo_shippedToCompleted() {
    assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.COMPLETED));
  }

  @Test
  void orderStatus_canTransitionTo_paidToCancelled() {
    assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELLED));
  }

  @Test
  void orderStatus_canNotTransitionTo_paidToShipped() {
    assertFalse(OrderStatus.PAID.canTransitionTo(OrderStatus.SHIPPED));
  }

  @Test
  void orderStatus_canNotTransitionTo_completedToAnything() {
    assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.CANCELLED));
    assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.PAID));
  }

  @Test
  void orderStatus_canNotTransitionTo_cancelledToAnything() {
    assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PAID));
  }
}
