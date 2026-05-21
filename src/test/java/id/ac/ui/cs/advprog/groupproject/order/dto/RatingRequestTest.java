package id.ac.ui.cs.advprog.groupproject.order.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RatingRequestTest {

    @Test
    void noArgConstructor_works() {
        RatingRequest request = new RatingRequest();
        request.setRatingProduk(5);
        request.setRatingJastiper(4);
        assertEquals(5, request.getRatingProduk());
        assertEquals(4, request.getRatingJastiper());
    }

    @Test
    void allArgConstructor_works() {
        RatingRequest request = new RatingRequest(3, 5);
        assertEquals(3, request.getRatingProduk());
        assertEquals(5, request.getRatingJastiper());
        assertNotNull(request);
    }

    @Test
    void allArgConstructor_withNulls_works() {
        RatingRequest request = new RatingRequest(4, null);
        assertEquals(4, request.getRatingProduk());
        assertNull(request.getRatingJastiper());
    }
}
