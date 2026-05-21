package id.ac.ui.cs.advprog.groupproject.order.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class CheckoutRequestTest {

    @Test
    void noArgConstructor_works() {
        CheckoutRequest request = new CheckoutRequest();
        assertNull(request.getBuyerId());
        assertNull(request.getProductId());
        assertNull(request.getQuantity());
        assertNull(request.getShippingAddress());
    }

    @Test
    void allArgConstructor_setsAllFields() {
        UUID buyerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CheckoutRequest request = new CheckoutRequest(buyerId, productId, 3, "Jl. Merdeka No. 1");

        assertEquals(buyerId, request.getBuyerId());
        assertEquals(productId, request.getProductId());
        assertEquals(3, request.getQuantity());
        assertEquals("Jl. Merdeka No. 1", request.getShippingAddress());
    }

    @Test
    void setters_work() {
        CheckoutRequest request = new CheckoutRequest();
        UUID buyerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        request.setBuyerId(buyerId);
        request.setProductId(productId);
        request.setQuantity(5);
        request.setShippingAddress("Jl. Sudirman No. 10");

        assertEquals(buyerId, request.getBuyerId());
        assertEquals(productId, request.getProductId());
        assertEquals(5, request.getQuantity());
        assertEquals("Jl. Sudirman No. 10", request.getShippingAddress());
    }
}
