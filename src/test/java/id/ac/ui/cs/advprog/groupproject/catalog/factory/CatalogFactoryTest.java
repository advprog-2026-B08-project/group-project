package id.ac.ui.cs.advprog.groupproject.catalog.factory;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.CreateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.UpdateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogFactoryTest {

    private CatalogFactory catalogFactory;

    @BeforeEach
    void setUp() {
        catalogFactory = new CatalogFactory();
    }

    @Test
    void testCreateMapsRequestFieldsAndJastiper() {
        LocalDate travelDate = LocalDate.of(2026, 9, 1);
        CreateCatalogRequest request = new CreateCatalogRequest(
            "Coffee Beans",
            "Single origin",
            "https://example.com/beans.jpg",
            180_000.0,
            12,
            "Melbourne",
            travelDate
        );

        User jastiper = new User();
        jastiper.setId(UUID.randomUUID());
        jastiper.setUsername("bean-hunter");
        jastiper.setRole("JASTIPER");

        Catalog result = catalogFactory.create(request, jastiper);

        assertEquals("Coffee Beans", result.getName());
        assertEquals("Single origin", result.getDescription());
        assertEquals("https://example.com/beans.jpg", result.getImageUrl());
        assertEquals(180_000.0, result.getPrice());
        assertEquals(12, result.getStock());
        assertEquals("Melbourne", result.getOriginLocation());
        assertEquals(travelDate, result.getTravelDate());
        assertEquals(jastiper, result.getJastiper());
    }

    @Test
    void testApplyUpdateUpdatesCatalogFields() {
        Catalog catalog = new Catalog();
        catalog.setName("Old Name");
        catalog.setDescription("Old Desc");
        catalog.setImageUrl("https://example.com/old.jpg");
        catalog.setPrice(100_000.0);
        catalog.setStock(1);
        catalog.setOriginLocation("Old City");
        catalog.setTravelDate(LocalDate.of(2026, 1, 1));

        UpdateCatalogRequest request = new UpdateCatalogRequest(
            "New Name",
            "New Desc",
            "https://example.com/new.jpg",
            220_000.0,
            7,
            "New City",
            LocalDate.of(2026, 12, 31)
        );

        catalogFactory.applyUpdate(catalog, request);

        assertEquals("New Name", catalog.getName());
        assertEquals("New Desc", catalog.getDescription());
        assertEquals("https://example.com/new.jpg", catalog.getImageUrl());
        assertEquals(220_000.0, catalog.getPrice());
        assertEquals(7, catalog.getStock());
        assertEquals("New City", catalog.getOriginLocation());
        assertEquals(LocalDate.of(2026, 12, 31), catalog.getTravelDate());
    }
}
