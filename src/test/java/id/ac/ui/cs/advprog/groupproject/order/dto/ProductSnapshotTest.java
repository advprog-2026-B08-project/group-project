package id.ac.ui.cs.advprog.groupproject.order.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ProductSnapshotTest {

    @Test
    void constructor_setsAllFields() {
        UUID jastiperId = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(75000);
        String name = "Sepatu Adidas";

        ProductSnapshot snapshot = new ProductSnapshot(jastiperId, price, name);

        assertEquals(jastiperId, snapshot.jastiperId());
        assertEquals(price, snapshot.pricePerItem());
        assertEquals(name, snapshot.productName());
    }

    @Test
    void record_equality_basedOnValues() {
        UUID jastiperId = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(50000);

        ProductSnapshot a = new ProductSnapshot(jastiperId, price, "Baju");
        ProductSnapshot b = new ProductSnapshot(jastiperId, price, "Baju");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void record_toString_containsValues() {
        UUID jastiperId = UUID.randomUUID();
        ProductSnapshot snapshot = new ProductSnapshot(jastiperId, BigDecimal.TEN, "Kaos");

        String str = snapshot.toString();
        assertTrue(str.contains("Kaos"));
    }
}
