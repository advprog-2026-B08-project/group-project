package id.ac.ui.cs.advprog.groupproject.order.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import id.ac.ui.cs.advprog.groupproject.order.dto.ProductSnapshot;

@ExtendWith(MockitoExtension.class)
class CatalogStockAdapterTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private CatalogStockAdapter catalogStockAdapter;

    private UUID productId;
    private Catalog catalog;
    private User jastiper;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        jastiper = new User();
        jastiper.setId(UUID.randomUUID());
        jastiper.setUsername("jastiper1");

        catalog = new Catalog();
        catalog.setId(productId);
        catalog.setName("Baju Bola");
        catalog.setPrice(50000.0);
        catalog.setStock(10);
        catalog.setJastiper(jastiper);
    }

    @Test
    void reserveStock_success_returnsProductSnapshotAndReducesStock() {
        when(catalogRepository.findById(productId)).thenReturn(Optional.of(catalog));
        when(catalogRepository.save(any(Catalog.class))).thenReturn(catalog);

        ProductSnapshot snapshot = catalogStockAdapter.reserveStock(productId, 3);

        assertNotNull(snapshot);
        assertEquals(jastiper.getId(), snapshot.jastiperId());
        assertEquals("Baju Bola", snapshot.productName());
        assertEquals(0, snapshot.pricePerItem().compareTo(java.math.BigDecimal.valueOf(50000.0)));
        // stock harus berkurang
        assertEquals(7, catalog.getStock());
        verify(catalogRepository).save(catalog);
    }

    @Test
    void reserveStock_productNotFound_throwsIllegalArgumentException() {
        when(catalogRepository.findById(productId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> catalogStockAdapter.reserveStock(productId, 2));

        assertTrue(ex.getMessage().contains("Product not found"));
        verify(catalogRepository, never()).save(any());
    }

    @Test
    void reserveStock_insufficientStock_throwsIllegalArgumentException() {
        catalog.setStock(2);
        when(catalogRepository.findById(productId)).thenReturn(Optional.of(catalog));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> catalogStockAdapter.reserveStock(productId, 5));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        // stock harus tidak berubah
        assertEquals(2, catalog.getStock());
        verify(catalogRepository, never()).save(any());
    }

    @Test
    void reserveStock_exactStock_success() {
        catalog.setStock(3);
        when(catalogRepository.findById(productId)).thenReturn(Optional.of(catalog));
        when(catalogRepository.save(any(Catalog.class))).thenReturn(catalog);

        ProductSnapshot snapshot = catalogStockAdapter.reserveStock(productId, 3);

        assertNotNull(snapshot);
        assertEquals(0, catalog.getStock());
    }

    @Test
    void releaseStock_success_increasesStock() {
        when(catalogRepository.findById(productId)).thenReturn(Optional.of(catalog));
        when(catalogRepository.save(any(Catalog.class))).thenReturn(catalog);

        catalogStockAdapter.releaseStock(productId, 4);

        assertEquals(14, catalog.getStock());
        verify(catalogRepository).save(catalog);
    }

    @Test
    void releaseStock_productNotFound_throwsIllegalArgumentException() {
        when(catalogRepository.findById(productId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> catalogStockAdapter.releaseStock(productId, 2));

        assertTrue(ex.getMessage().contains("Product not found"));
        verify(catalogRepository, never()).save(any());
    }
}
