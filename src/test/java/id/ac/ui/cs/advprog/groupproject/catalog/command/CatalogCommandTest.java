package id.ac.ui.cs.advprog.groupproject.catalog.command;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogCommandTest {

    @Test
    void testCreateCatalogCommandStoresAllValues() {
        LocalDate travelDate = LocalDate.of(2026, 7, 10);

        CreateCatalogCommand command = new CreateCatalogCommand(
            "Headphone",
            "Noise cancelling",
            "https://example.com/headphone.jpg",
            1_250_000.0,
            4,
            "Singapore",
            travelDate
        );

        assertEquals("Headphone", command.getName());
        assertEquals("Noise cancelling", command.getDescription());
        assertEquals("https://example.com/headphone.jpg", command.getImageUrl());
        assertEquals(1_250_000.0, command.getPrice());
        assertEquals(4, command.getStock());
        assertEquals("Singapore", command.getOriginLocation());
        assertEquals(travelDate, command.getTravelDate());
    }

    @Test
    void testUpdateCatalogCommandStoresAllValues() {
        LocalDate travelDate = LocalDate.of(2026, 8, 15);

        UpdateCatalogCommand command = new UpdateCatalogCommand(
            "Sneakers",
            "Limited edition",
            "https://example.com/sneakers.jpg",
            2_400_000.0,
            2,
            "Osaka",
            travelDate
        );

        assertEquals("Sneakers", command.getName());
        assertEquals("Limited edition", command.getDescription());
        assertEquals("https://example.com/sneakers.jpg", command.getImageUrl());
        assertEquals(2_400_000.0, command.getPrice());
        assertEquals(2, command.getStock());
        assertEquals("Osaka", command.getOriginLocation());
        assertEquals(travelDate, command.getTravelDate());
    }
}
