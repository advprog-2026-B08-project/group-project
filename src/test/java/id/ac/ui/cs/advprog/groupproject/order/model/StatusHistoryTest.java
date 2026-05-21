package id.ac.ui.cs.advprog.groupproject.order.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class StatusHistoryTest {

    @Test
    void settersAndGetters_work() {
        StatusHistory history = new StatusHistory();

        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(UUID.randomUUID());
        LocalDateTime now = LocalDateTime.now();

        history.setId(id);
        history.setOrder(order);
        history.setStatus(OrderStatus.PAID);
        history.setTimestamp(now);

        assertEquals(id, history.getId());
        assertEquals(order, history.getOrder());
        assertEquals(OrderStatus.PAID, history.getStatus());
        assertEquals(now, history.getTimestamp());
    }

    @Test
    void defaultTimestamp_isNotNull() {
        StatusHistory history = new StatusHistory();
        // timestamp di-inisialisasi langsung di field declaration
        assertNotNull(history.getTimestamp());
    }

    @Test
    void status_canBeSetToAnyOrderStatus() {
        StatusHistory history = new StatusHistory();

        for (OrderStatus status : OrderStatus.values()) {
            history.setStatus(status);
            assertEquals(status, history.getStatus());
        }
    }
}
