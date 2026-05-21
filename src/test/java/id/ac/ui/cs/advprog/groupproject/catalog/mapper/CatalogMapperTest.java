package id.ac.ui.cs.advprog.groupproject.catalog.mapper;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.CatalogDto;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.CreateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.UpdateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CatalogMapperTest {

    private CatalogMapper catalogMapper;

    @BeforeEach
    void setUp() {
        catalogMapper = new CatalogMapper();
    }

    @Test
    void testToDtoMapsAllCatalogFieldsIncludingJastiper() {
        UUID catalogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate travelDate = LocalDate.of(2026, 5, 20);

        User jastiper = new User();
        jastiper.setId(userId);
        jastiper.setUsername("catalog-owner");

        Catalog catalog = new Catalog();
        catalog.setId(catalogId);
        catalog.setJastiper(jastiper);
        catalog.setName("Smart Watch");
        catalog.setDescription("Water resistant");
        catalog.setImageUrl("https://example.com/watch.jpg");
        catalog.setPrice(2_000_000.0);
        catalog.setStock(6);
        catalog.setRatingAverage(4.6);
        catalog.setRatingCount(11);
        catalog.setOriginLocation("Busan");
        catalog.setTravelDate(travelDate);

        CatalogDto dto = catalogMapper.toDto(catalog);

        assertEquals(catalogId, dto.getId());
        assertEquals(userId, dto.getJastiperId());
        assertEquals("catalog-owner", dto.getJastiperUsername());
        assertEquals("Smart Watch", dto.getName());
        assertEquals("Water resistant", dto.getDescription());
        assertEquals("https://example.com/watch.jpg", dto.getImageUrl());
        assertEquals(2_000_000.0, dto.getPrice());
        assertEquals(6, dto.getStock());
        assertEquals(4.6, dto.getRatingAverage());
        assertEquals(11, dto.getRatingCount());
        assertEquals("Busan", dto.getOriginLocation());
        assertEquals(travelDate, dto.getTravelDate());
    }

    @Test
    void testToDtoDoesNotSetJastiperFieldsWhenJastiperIsNull() {
        Catalog catalog = new Catalog();
        catalog.setId(UUID.randomUUID());
        catalog.setName("Camera");
        catalog.setPrice(999_000.0);
        catalog.setStock(2);
        catalog.setOriginLocation("Tokyo");
        catalog.setTravelDate(LocalDate.of(2026, 11, 11));

        CatalogDto dto = catalogMapper.toDto(catalog);

        assertNull(dto.getJastiperId());
        assertNull(dto.getJastiperUsername());
        assertEquals("Camera", dto.getName());
    }

    @Test
    void testToDtoListMapsEachCatalog() {
        Catalog first = new Catalog();
        first.setId(UUID.randomUUID());
        first.setName("A");
        first.setPrice(10.0);
        first.setStock(1);
        first.setOriginLocation("X");
        first.setTravelDate(LocalDate.of(2026, 1, 1));

        Catalog second = new Catalog();
        second.setId(UUID.randomUUID());
        second.setName("B");
        second.setPrice(20.0);
        second.setStock(2);
        second.setOriginLocation("Y");
        second.setTravelDate(LocalDate.of(2026, 2, 2));

        List<CatalogDto> dtoList = catalogMapper.toDtoList(List.of(first, second));

        assertEquals(2, dtoList.size());
        assertEquals("A", dtoList.get(0).getName());
        assertEquals("B", dtoList.get(1).getName());
    }

    @Test
    void testToCreateRequestMapsAllDtoFields() {
        CatalogDto dto = createDto();

        CreateCatalogRequest request = catalogMapper.toCreateRequest(dto);

        assertEquals(dto.getName(), request.getName());
        assertEquals(dto.getDescription(), request.getDescription());
        assertEquals(dto.getImageUrl(), request.getImageUrl());
        assertEquals(dto.getPrice(), request.getPrice());
        assertEquals(dto.getStock(), request.getStock());
        assertEquals(dto.getOriginLocation(), request.getOriginLocation());
        assertEquals(dto.getTravelDate(), request.getTravelDate());
    }

    @Test
    void testToUpdateRequestMapsAllDtoFields() {
        CatalogDto dto = createDto();

        UpdateCatalogRequest request = catalogMapper.toUpdateRequest(dto);

        assertEquals(dto.getName(), request.getName());
        assertEquals(dto.getDescription(), request.getDescription());
        assertEquals(dto.getImageUrl(), request.getImageUrl());
        assertEquals(dto.getPrice(), request.getPrice());
        assertEquals(dto.getStock(), request.getStock());
        assertEquals(dto.getOriginLocation(), request.getOriginLocation());
        assertEquals(dto.getTravelDate(), request.getTravelDate());
    }

    private CatalogDto createDto() {
        CatalogDto dto = new CatalogDto();
        dto.setName("Mechanical Keyboard");
        dto.setDescription("75% layout");
        dto.setImageUrl("https://example.com/keyboard.jpg");
        dto.setPrice(1_500_000.0);
        dto.setStock(5);
        dto.setOriginLocation("Taipei");
        dto.setTravelDate(LocalDate.of(2026, 10, 10));
        return dto;
    }
}
