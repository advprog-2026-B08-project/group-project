package id.ac.ui.cs.advprog.groupproject.service;

import id.ac.ui.cs.advprog.groupproject.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.model.User;
import id.ac.ui.cs.advprog.groupproject.repository.CatalogRepository;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class CatalogServiceTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private CatalogService catalogService;

    private User testUser;
    private User anotherUser;
    private Catalog testCatalog;
    private UUID catalogId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setRole("JASTIPER");

        anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setUsername("anotheruser");
        anotherUser.setRole("JASTIPER");

        catalogId = UUID.randomUUID();
        testCatalog = new Catalog();
        testCatalog.setId(catalogId);
        testCatalog.setName("Test Product");
        testCatalog.setDescription("Test Description");
        testCatalog.setImageUrl("http://example.com/image.jpg");
        testCatalog.setPrice(100.0);
        testCatalog.setStock(10);
        testCatalog.setOriginLocation("Jakarta");
        testCatalog.setTravelDate(LocalDate.now().plusDays(7));
        testCatalog.setJastiper(testUser);
    }

    @Test
    void testCreateCatalog() {
        Catalog newCatalog = new Catalog();
        newCatalog.setName("New Product");
        newCatalog.setPrice(200.0);

        when(catalogRepository.save(any(Catalog.class))).thenReturn(newCatalog);

        Catalog result = catalogService.createCatalog(newCatalog, testUser);

        assertNotNull(result);
        assertEquals(testUser, newCatalog.getJastiper());
        verify(catalogRepository, times(1)).save(newCatalog);
    }

    @Test
    void testFindAllCatalogs() {
        List<Catalog> catalogList = new ArrayList<>();
        catalogList.add(testCatalog);
        
        when(catalogRepository.findByJastiperId(userId)).thenReturn(catalogList);

        List<Catalog> result = catalogService.findAllCatalogs(testUser);

        assertEquals(1, result.size());
        assertEquals(testCatalog, result.get(0));
        verify(catalogRepository, times(1)).findByJastiperId(userId);
    }

    @Test
    void testGetAllCatalogs() {
        List<Catalog> catalogList = new ArrayList<>();
        catalogList.add(testCatalog);
        catalogList.add(new Catalog());

        when(catalogRepository.findAll()).thenReturn(catalogList);

        List<Catalog> result = catalogService.getAllCatalogs();

        assertEquals(2, result.size());
        verify(catalogRepository, times(1)).findAll();
    }

    @Test
    void testGetCatalogsByUserId() {
        List<Catalog> catalogList = new ArrayList<>();
        catalogList.add(testCatalog);

        when(catalogRepository.findByJastiperId(userId)).thenReturn(catalogList);

        List<Catalog> result = catalogService.getCatalogsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(testCatalog, result.get(0));
        verify(catalogRepository, times(1)).findByJastiperId(userId);
    }

    @Test
    void testGetCatalogByIdSuccess() {
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));

        Catalog result = catalogService.getCatalogById(catalogId, testUser);

        assertEquals(testCatalog, result);
        verify(catalogRepository, times(1)).findById(catalogId);
    }

    @Test
    void testGetCatalogByIdNotFound() {
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            catalogService.getCatalogById(catalogId, testUser);
        });

        verify(catalogRepository, times(1)).findById(catalogId);
    }

    @Test
    void testGetCatalogByIdForbidden() {
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));

        assertThrows(ResponseStatusException.class, () -> {
            catalogService.getCatalogById(catalogId, anotherUser);
        });

        verify(catalogRepository, times(1)).findById(catalogId);
    }

    @Test
    void testUpdateCatalogSuccess() {
        Catalog updatedData = new Catalog();
        updatedData.setName("Updated Product");
        updatedData.setDescription("Updated Description");
        updatedData.setImageUrl("http://example.com/updated.jpg");
        updatedData.setPrice(150.0);
        updatedData.setStock(20);
        updatedData.setOriginLocation("Bandung");
        updatedData.setTravelDate(LocalDate.now().plusDays(14));

        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));
        when(catalogRepository.save(any(Catalog.class))).thenReturn(testCatalog);

        Catalog result = catalogService.updateCatalog(catalogId, updatedData, testUser);

        assertNotNull(result);
        assertEquals("Updated Product", testCatalog.getName());
        assertEquals("Updated Description", testCatalog.getDescription());
        assertEquals("http://example.com/updated.jpg", testCatalog.getImageUrl());
        assertEquals(150.0, testCatalog.getPrice());
        assertEquals(20, testCatalog.getStock());
        assertEquals("Bandung", testCatalog.getOriginLocation());
        verify(catalogRepository, times(1)).findById(catalogId);
        verify(catalogRepository, times(1)).save(testCatalog);
    }

    @Test
    void testUpdateCatalogNotFound() {
        Catalog updatedData = new Catalog();
        updatedData.setName("Updated Product");

        when(catalogRepository.findById(catalogId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            catalogService.updateCatalog(catalogId, updatedData, testUser);
        });

        verify(catalogRepository, times(1)).findById(catalogId);
        verify(catalogRepository, never()).save(any());
    }

    @Test
    void testUpdateCatalogForbidden() {
        Catalog updatedData = new Catalog();
        updatedData.setName("Updated Product");

        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));

        assertThrows(ResponseStatusException.class, () -> {
            catalogService.updateCatalog(catalogId, updatedData, anotherUser);
        });

        verify(catalogRepository, times(1)).findById(catalogId);
        verify(catalogRepository, never()).save(any());
    }

    @Test
    void testDeleteCatalogSuccess() {
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));
        doNothing().when(catalogRepository).deleteById(catalogId);

        catalogService.deleteCatalog(catalogId, testUser);

        verify(catalogRepository, times(1)).findById(catalogId);
        verify(catalogRepository, times(1)).deleteById(catalogId);
    }

    @Test
    void testDeleteCatalogNotFound() {
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            catalogService.deleteCatalog(catalogId, testUser);
        });

        verify(catalogRepository, times(1)).findById(catalogId);
        verify(catalogRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteCatalogForbidden() {
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));

        assertThrows(ResponseStatusException.class, () -> {
            catalogService.deleteCatalog(catalogId, anotherUser);
        });

        verify(catalogRepository, times(1)).findById(catalogId);
        verify(catalogRepository, never()).deleteById(any());
    }
}
