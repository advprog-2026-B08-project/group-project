package id.ac.ui.cs.advprog.groupproject.catalog.service;

import id.ac.ui.cs.advprog.groupproject.catalog.command.CreateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.command.UpdateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.factory.CatalogFactory;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private CatalogRepository catalogRepository;

    @Mock
    private CatalogFactory catalogFactory;

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
        CreateCatalogCommand command = new CreateCatalogCommand(
            "New Product",
            "New Description",
            "http://example.com/new.jpg",
            200.0,
            5,
            "Jakarta",
            LocalDate.now().plusDays(10)
        );

        Catalog newCatalog = new Catalog();
        newCatalog.setName("New Product");
        newCatalog.setPrice(200.0);
        newCatalog.setJastiper(testUser);

        when(catalogFactory.create(command, testUser)).thenReturn(newCatalog);
        when(catalogRepository.save(any(Catalog.class))).thenReturn(newCatalog);

        Catalog result = catalogService.createCatalog(command, testUser);

        assertNotNull(result);
        verify(catalogFactory, times(1)).create(command, testUser);
        verify(catalogRepository, times(1)).save(newCatalog);
    }

    @Test
    void testCreateCatalogForbiddenForNonJastiper() {
        CreateCatalogCommand command = new CreateCatalogCommand(
            "New Product",
            "New Description",
            "http://example.com/new.jpg",
            200.0,
            5,
            "Jakarta",
            LocalDate.now().plusDays(10)
        );

        User customerUser = new User();
        customerUser.setId(UUID.randomUUID());
        customerUser.setUsername("customer");
        customerUser.setRole("CUSTOMER");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            catalogService.createCatalog(command, customerUser);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(catalogFactory, never()).create(any(CreateCatalogCommand.class), any(User.class));
        verify(catalogRepository, never()).save(any(Catalog.class));
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
    void testSearchCatalogs() {
        List<Catalog> catalogList = new ArrayList<>();
        catalogList.add(testCatalog);

        when(catalogRepository.searchCatalogs("test", "seller")).thenReturn(catalogList);

        List<Catalog> result = catalogService.searchCatalogs("test", "seller");

        assertEquals(1, result.size());
        assertEquals(testCatalog, result.get(0));
        verify(catalogRepository, times(1)).searchCatalogs("test", "seller");
    }

    @Test
    void testSearchCatalogsByKeyword() {
        List<Catalog> catalogList = new ArrayList<>();
        catalogList.add(testCatalog);

        when(catalogRepository.searchCatalogsByKeyword("test")).thenReturn(catalogList);

        List<Catalog> result = catalogService.searchCatalogs("test");

        assertEquals(1, result.size());
        assertEquals(testCatalog, result.get(0));
        verify(catalogRepository, times(1)).searchCatalogsByKeyword("test");
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
        UpdateCatalogCommand command = new UpdateCatalogCommand(
            "Updated Product",
            "Updated Description",
            "http://example.com/updated.jpg",
            150.0,
            20,
            "Bandung",
            LocalDate.now().plusDays(14)
        );

        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));
        when(catalogRepository.save(any(Catalog.class))).thenReturn(testCatalog);

        doAnswer(invocation -> {
            Catalog target = invocation.getArgument(0);
            UpdateCatalogCommand cmd = invocation.getArgument(1);
            target.setName(cmd.getName());
            target.setDescription(cmd.getDescription());
            target.setImageUrl(cmd.getImageUrl());
            target.setPrice(cmd.getPrice());
            target.setStock(cmd.getStock());
            target.setOriginLocation(cmd.getOriginLocation());
            target.setTravelDate(cmd.getTravelDate());
            return null;
        }).when(catalogFactory).applyUpdate(any(Catalog.class), any(UpdateCatalogCommand.class));

        Catalog result = catalogService.updateCatalog(catalogId, command, testUser);

        assertNotNull(result);
        verify(catalogRepository, times(1)).findById(catalogId);
        verify(catalogFactory, times(1)).applyUpdate(testCatalog, command);
        verify(catalogRepository, times(1)).save(testCatalog);
    }

    @Test
    void testUpdateCatalogNotFound() {
        UpdateCatalogCommand command = new UpdateCatalogCommand(
            "Updated Product",
            "Updated Description",
            "http://example.com/updated.jpg",
            150.0,
            20,
            "Bandung",
            LocalDate.now().plusDays(14)
        );

        when(catalogRepository.findById(catalogId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            catalogService.updateCatalog(catalogId, command, testUser);
        });

        verify(catalogRepository, times(1)).findById(catalogId);
        verify(catalogFactory, never()).applyUpdate(any(), any());
        verify(catalogRepository, never()).save(any());
    }

    @Test
    void testUpdateCatalogForbidden() {
        UpdateCatalogCommand command = new UpdateCatalogCommand(
            "Updated Product",
            "Updated Description",
            "http://example.com/updated.jpg",
            150.0,
            20,
            "Bandung",
            LocalDate.now().plusDays(14)
        );

        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));

        assertThrows(ResponseStatusException.class, () -> {
            catalogService.updateCatalog(catalogId, command, anotherUser);
        });

        verify(catalogRepository, times(1)).findById(catalogId);
        verify(catalogFactory, never()).applyUpdate(any(), any());
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

    @Test
    void testDecreaseStockSuccess() {
        when(catalogRepository.decreaseStockIfAvailable(catalogId, 3)).thenReturn(1);
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));

        Catalog result = catalogService.decreaseStock(catalogId, 3);

        assertNotNull(result);
        assertEquals(10, result.getStock());
        verify(catalogRepository, times(1)).decreaseStockIfAvailable(catalogId, 3);
        verify(catalogRepository, times(1)).findById(catalogId);
        verify(catalogRepository, never()).save(any(Catalog.class));
    }

    @Test
    void testDecreaseStockBadRequestWhenQuantityInvalid() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            catalogService.decreaseStock(catalogId, 0);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(catalogRepository, never()).decreaseStockIfAvailable(any(UUID.class), anyInt());
        verify(catalogRepository, never()).findById(any(UUID.class));
        verify(catalogRepository, never()).save(any(Catalog.class));
    }

    @Test
    void testDecreaseStockNotFound() {
        when(catalogRepository.decreaseStockIfAvailable(catalogId, 2)).thenReturn(0);
        when(catalogRepository.existsById(catalogId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            catalogService.decreaseStock(catalogId, 2);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(catalogRepository, times(1)).decreaseStockIfAvailable(catalogId, 2);
        verify(catalogRepository, times(1)).existsById(catalogId);
        verify(catalogRepository, never()).findById(catalogId);
        verify(catalogRepository, never()).save(any(Catalog.class));
    }

    @Test
    void testDecreaseStockConflictWhenInsufficientStock() {
        when(catalogRepository.decreaseStockIfAvailable(catalogId, 999)).thenReturn(0);
        when(catalogRepository.existsById(catalogId)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            catalogService.decreaseStock(catalogId, 999);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(catalogRepository, times(1)).decreaseStockIfAvailable(catalogId, 999);
        verify(catalogRepository, times(1)).existsById(catalogId);
        verify(catalogRepository, never()).findById(catalogId);
        verify(catalogRepository, never()).save(any(Catalog.class));
    }
}
