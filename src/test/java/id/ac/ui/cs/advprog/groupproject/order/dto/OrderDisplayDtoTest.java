package id.ac.ui.cs.advprog.groupproject.order.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;

class OrderDisplayDtoTest {

    @Test
    void from_mapsAllFieldsCorrectly() {
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
        order.setRatingProduk(4);

        OrderDisplayDto dto = OrderDisplayDto.from(order);

        assertEquals(id, dto.getId());
        assertEquals(buyerId, dto.getBuyerId());
        assertEquals(jastiperId, dto.getJastiperId());
        assertEquals(productId, dto.getProductId());
        assertEquals(3, dto.getQuantity());
        assertEquals("Jl. Test No. 1", dto.getShippingAddress());
        assertEquals(BigDecimal.valueOf(150000), dto.getTotalPrice());
        assertEquals(OrderStatus.PAID, dto.getStatus());
        assertEquals(4, dto.getRatingProduk());
        // Enriched fields should be null by default
        assertNull(dto.getProductName());
        assertNull(dto.getProductImageUrl());
        assertNull(dto.getBuyerUsername());
        assertNull(dto.getJastiperUsername());
    }

    @Test
    void enrichedFields_canBeSet() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setBuyerId(UUID.randomUUID());
        order.setJastiperId(UUID.randomUUID());
        order.setProductId(UUID.randomUUID());
        order.setQuantity(1);
        order.setTotalPrice(BigDecimal.ZERO);
        order.setStatus(OrderStatus.COMPLETED);

        OrderDisplayDto dto = OrderDisplayDto.from(order);
        dto.setProductName("Baju Bola");
        dto.setProductImageUrl("https://example.com/img.jpg");
        dto.setBuyerUsername("titiper1");
        dto.setJastiperUsername("jastiper1");

        assertEquals("Baju Bola", dto.getProductName());
        assertEquals("https://example.com/img.jpg", dto.getProductImageUrl());
        assertEquals("titiper1", dto.getBuyerUsername());
        assertEquals("jastiper1", dto.getJastiperUsername());
    }
}
