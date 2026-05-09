package id.ac.ui.cs.advprog.groupproject.order.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderEqualsHashCodeTest {

    @Test
    void equals_sameId_returnsTrue() {
        UUID id = UUID.randomUUID();
        Order a = new Order();
        a.setId(id);
        Order b = new Order();
        b.setId(id);

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    @Test
    void equals_differentId_returnsFalse() {
        Order a = new Order();
        a.setId(UUID.randomUUID());
        Order b = new Order();
        b.setId(UUID.randomUUID());

        assertFalse(a.equals(b));
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        Order a = new Order();
        a.setId(UUID.randomUUID());
        assertTrue(a.equals(a));
    }

    @Test
    void equals_null_returnsFalse() {
        Order a = new Order();
        a.setId(UUID.randomUUID());
        assertFalse(a.equals(null));
    }

    @Test
    void equals_differentClass_returnsFalse() {
        Order a = new Order();
        a.setId(UUID.randomUUID());
        assertFalse(a.equals("not an order"));
    }

    @Test
    void equals_nullId_returnsFalse() {
        Order a = new Order();
        Order b = new Order();
        // Both have null IDs
        assertFalse(a.equals(b));
    }

    @Test
    void hashCode_sameId_sameHash() {
        UUID id = UUID.randomUUID();
        Order a = new Order();
        a.setId(id);
        Order b = new Order();
        b.setId(id);

        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCode_nullId_doesNotThrow() {
        Order a = new Order();
        // Should not throw, uses identity hash
        int hash = a.hashCode();
        assertTrue(hash != 0 || hash == 0); // just verify it returns
    }
}
