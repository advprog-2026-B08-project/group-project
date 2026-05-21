package id.ac.ui.cs.advprog.groupproject.catalog.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogRequestTest {

    @Test
    void testCreateCatalogRequestStoresAllValues() {
        LocalDate travelDate = LocalDate.of(2026, 7, 10);

        CreateCatalogRequest request = new CreateCatalogRequest(
            "Headphone",
            "Noise cancelling",
            "https://example.com/headphone.jpg",
            1_250_000.0,
            4,
            "Singapore",
            travelDate
        );

        assertEquals("Headphone", request.getName());
        assertEquals("Noise cancelling", request.getDescription());
        assertEquals("https://example.com/headphone.jpg", request.getImageUrl());
        assertEquals(1_250_000.0, request.getPrice());
        assertEquals(4, request.getStock());
        assertEquals("Singapore", request.getOriginLocation());
        assertEquals(travelDate, request.getTravelDate());
    }

    @Test
    void testUpdateCatalogRequestStoresAllValues() {
        LocalDate travelDate = LocalDate.of(2026, 8, 15);

        UpdateCatalogRequest request = new UpdateCatalogRequest(
            "Sneakers",
            "Limited edition",
            "https://example.com/sneakers.jpg",
            2_400_000.0,
            2,
            "Osaka",
            travelDate
        );

        assertEquals("Sneakers", request.getName());
        assertEquals("Limited edition", request.getDescription());
        assertEquals("https://example.com/sneakers.jpg", request.getImageUrl());
        assertEquals(2_400_000.0, request.getPrice());
        assertEquals(2, request.getStock());
        assertEquals("Osaka", request.getOriginLocation());
        assertEquals(travelDate, request.getTravelDate());
    }
}
