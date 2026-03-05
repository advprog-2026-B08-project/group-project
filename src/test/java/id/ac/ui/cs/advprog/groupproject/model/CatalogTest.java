package id.ac.ui.cs.advprog.groupproject.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogTest {

    private Validator validator;
    private User testUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setRole("JASTIPER");
    }

    @Test
    void testValidCatalog() {
        Catalog catalog = new Catalog();
        catalog.setId(UUID.randomUUID());
        catalog.setName("Test Product");
        catalog.setDescription("Test Description");
        catalog.setImageUrl("http://example.com/image.jpg");
        catalog.setPrice(100.0);
        catalog.setStock(10);
        catalog.setOriginLocation("Jakarta");
        catalog.setTravelDate(LocalDate.now().plusDays(7));
        catalog.setJastiper(testUser);

        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCatalogNameBlank() {
        Catalog catalog = new Catalog();
        catalog.setName("");
        catalog.setPrice(100.0);
        catalog.setStock(10);
        catalog.setOriginLocation("Jakarta");
        catalog.setTravelDate(LocalDate.now().plusDays(7));

        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Product name is required")));
    }

    @Test
    void testCatalogPriceNull() {
        Catalog catalog = new Catalog();
        catalog.setName("Test Product");
        catalog.setPrice(null);
        catalog.setStock(10);
        catalog.setOriginLocation("Jakarta");
        catalog.setTravelDate(LocalDate.now().plusDays(7));

        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Price is required")));
    }

    @Test
    void testCatalogPriceNegative() {
        Catalog catalog = new Catalog();
        catalog.setName("Test Product");
        catalog.setPrice(-10.0);
        catalog.setStock(10);
        catalog.setOriginLocation("Jakarta");
        catalog.setTravelDate(LocalDate.now().plusDays(7));

        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Price cannot be negative")));
    }

    @Test
    void testCatalogStockNull() {
        Catalog catalog = new Catalog();
        catalog.setName("Test Product");
        catalog.setPrice(100.0);
        catalog.setStock(null);
        catalog.setOriginLocation("Jakarta");
        catalog.setTravelDate(LocalDate.now().plusDays(7));

        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Stock is required")));
    }

    @Test
    void testCatalogStockNegative() {
        Catalog catalog = new Catalog();
        catalog.setName("Test Product");
        catalog.setPrice(100.0);
        catalog.setStock(-5);
        catalog.setOriginLocation("Jakarta");
        catalog.setTravelDate(LocalDate.now().plusDays(7));

        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Stock cannot be negative")));
    }

    @Test
    void testCatalogGettersAndSetters() {
        Catalog catalog = new Catalog();
        UUID id = UUID.randomUUID();
        LocalDate travelDate = LocalDate.now().plusDays(7);

        catalog.setId(id);
        catalog.setName("Product");
        catalog.setDescription("Description");
        catalog.setImageUrl("http://example.com/image.jpg");
        catalog.setPrice(200.0);
        catalog.setStock(20);
        catalog.setOriginLocation("Bandung");
        catalog.setTravelDate(travelDate);
        catalog.setJastiper(testUser);

        assertEquals(id, catalog.getId());
        assertEquals("Product", catalog.getName());
        assertEquals("Description", catalog.getDescription());
        assertEquals("http://example.com/image.jpg", catalog.getImageUrl());
        assertEquals(200.0, catalog.getPrice());
        assertEquals(20, catalog.getStock());
        assertEquals("Bandung", catalog.getOriginLocation());
        assertEquals(travelDate, catalog.getTravelDate());
        assertEquals(testUser, catalog.getJastiper());
    }

    @Test
    void testCatalogOptionalFields() {
        Catalog catalog = new Catalog();
        catalog.setName("Test Product");
        catalog.setPrice(100.0);
        catalog.setStock(10);
        catalog.setOriginLocation("Jakarta");
        catalog.setTravelDate(LocalDate.now().plusDays(7));
        Set<ConstraintViolation<Catalog>> violations = validator.validate(catalog);
        assertTrue(violations.isEmpty());
    }
}
