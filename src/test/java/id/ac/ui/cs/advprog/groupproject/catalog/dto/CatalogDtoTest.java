package id.ac.ui.cs.advprog.groupproject.catalog.dto;

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

class CatalogDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCatalogDtoHasNoValidationViolations() {
        CatalogDto dto = createValidDto();

        Set<ConstraintViolation<CatalogDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testCatalogDtoNameBlankHasValidationViolation() {
        CatalogDto dto = createValidDto();
        dto.setName(" ");

        Set<ConstraintViolation<CatalogDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Product name is required")));
    }

    @Test
    void testCatalogDtoNegativePriceHasValidationViolation() {
        CatalogDto dto = createValidDto();
        dto.setPrice(-1.0);

        Set<ConstraintViolation<CatalogDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Price cannot be negative")));
    }

    @Test
    void testCatalogDtoNullStockHasValidationViolation() {
        CatalogDto dto = createValidDto();
        dto.setStock(null);

        Set<ConstraintViolation<CatalogDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Stock is required")));
    }

    @Test
    void testCatalogDtoGettersAndSetters() {
        UUID id = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();
        LocalDate travelDate = LocalDate.of(2026, 6, 1);

        CatalogDto dto = new CatalogDto();
        dto.setId(id);
        dto.setJastiperId(jastiperId);
        dto.setJastiperUsername("jastiper-user");
        dto.setName("Rice Cooker");
        dto.setDescription("Low watt");
        dto.setImageUrl("https://example.com/img.jpg");
        dto.setPrice(250_000.0);
        dto.setStock(3);
        dto.setOriginLocation("Tokyo");
        dto.setTravelDate(travelDate);

        assertEquals(id, dto.getId());
        assertEquals(jastiperId, dto.getJastiperId());
        assertEquals("jastiper-user", dto.getJastiperUsername());
        assertEquals("Rice Cooker", dto.getName());
        assertEquals("Low watt", dto.getDescription());
        assertEquals("https://example.com/img.jpg", dto.getImageUrl());
        assertEquals(250_000.0, dto.getPrice());
        assertEquals(3, dto.getStock());
        assertEquals("Tokyo", dto.getOriginLocation());
        assertEquals(travelDate, dto.getTravelDate());
    }

    private CatalogDto createValidDto() {
        CatalogDto dto = new CatalogDto();
        dto.setId(UUID.randomUUID());
        dto.setJastiperId(UUID.randomUUID());
        dto.setJastiperUsername("testuser");
        dto.setName("Portable Charger");
        dto.setDescription("20.000 mAh");
        dto.setImageUrl("https://example.com/charger.jpg");
        dto.setPrice(199_000.0);
        dto.setStock(8);
        dto.setOriginLocation("Seoul");
        dto.setTravelDate(LocalDate.now().plusDays(5));
        return dto;
    }
}
