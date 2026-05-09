package id.ac.ui.cs.advprog.groupproject.order.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class RatingRequestTest {

    @Test
    void noArgConstructor_works() {
        RatingRequest request = new RatingRequest();
        request.setRatingProduk(5);
        assertEquals(5, request.getRatingProduk());
    }

    @Test
    void allArgConstructor_works() {
        RatingRequest request = new RatingRequest(3);
        assertEquals(3, request.getRatingProduk());
        assertNotNull(request);
    }
}
