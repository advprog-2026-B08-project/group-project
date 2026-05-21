package id.ac.ui.cs.advprog.groupproject.catalog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.ActionLogService;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.CreateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.UpdateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.factory.CatalogFactory;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.catalog.model.CatalogRatingEvent;
import id.ac.ui.cs.advprog.groupproject.catalog.model.StockDecreaseEvent;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRatingEventRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.StockDecreaseEventRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.strategy.CatalogActionStrategy;
import id.ac.ui.cs.advprog.groupproject.catalog.strategy.DefaultCatalogStrategy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private CatalogRepository catalogRepository;

    @Mock
    private CatalogFactory catalogFactory;

    @Mock
    private CatalogRatingEventRepository catalogRatingEventRepository;

    @Mock
    private StockDecreaseEventRepository stockDecreaseEventRepository;

    @Spy
    private CatalogActionStrategy catalogStrategy = new DefaultCatalogStrategy();

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @Mock
    private ActionLogService actionLogService;

    @InjectMocks
    private CatalogService catalogService;

    private User testUser;
    private User anotherUser;
    private User titiperUser;
    private Catalog testCatalog;
    private UUID catalogId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
        lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);

        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setRole("JASTIPER");

        anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setUsername("anotheruser");
        anotherUser.setRole("JASTIPER");

        titiperUser = new User();
        titiperUser.setId(UUID.randomUUID());
        titiperUser.setUsername("titiper");
        titiperUser.setRole("ROLE_TITIPER");

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
        CreateCatalogRequest request = new CreateCatalogRequest(
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

        when(catalogFactory.create(request, testUser)).thenReturn(newCatalog);
        when(catalogRepository.save(any(Catalog.class))).thenReturn(newCatalog);

        Catalog result = catalogService.createCatalog(request, testUser);

        assertNotNull(result);
        verify(catalogFactory, times(1)).create(request, testUser);
        verify(catalogRepository, times(1)).save(newCatalog);
    }

    @Test
    void testCreateCatalogForbiddenForNonJastiper() {
        CreateCatalogRequest request = new CreateCatalogRequest(
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
            catalogService.createCatalog(request, customerUser);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(catalogFactory, never()).create(any(CreateCatalogRequest.class), any(User.class));
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
        UpdateCatalogRequest command = new UpdateCatalogRequest(
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
            UpdateCatalogRequest cmd = invocation.getArgument(1);
            target.setName(cmd.getName());
            target.setDescription(cmd.getDescription());
            target.setImageUrl(cmd.getImageUrl());
            target.setPrice(cmd.getPrice());
            target.setStock(cmd.getStock());
            target.setOriginLocation(cmd.getOriginLocation());
            target.setTravelDate(cmd.getTravelDate());
            return null;
        }).when(catalogFactory).applyUpdate(any(Catalog.class), any(UpdateCatalogRequest.class));

        Catalog result = catalogService.updateCatalog(catalogId, command, testUser);

        assertNotNull(result);
        verify(catalogRepository, times(1)).findById(catalogId);
        verify(catalogFactory, times(1)).applyUpdate(testCatalog, command);
        verify(catalogRepository, times(1)).save(testCatalog);
    }

    @Test
    void testUpdateCatalogNotFound() {
        UpdateCatalogRequest command = new UpdateCatalogRequest(
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
        UpdateCatalogRequest command = new UpdateCatalogRequest(
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
        UUID requestId = UUID.randomUUID();
        when(stockDecreaseEventRepository.existsByRequestId(requestId)).thenReturn(false);
        when(stockDecreaseEventRepository.saveAndFlush(any(StockDecreaseEvent.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(catalogRepository.decreaseStockIfAvailable(catalogId, 3)).thenReturn(1);
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));

        Catalog result = catalogService.decreaseStock(catalogId, requestId, 3);

        assertNotNull(result);
        assertEquals(10, result.getStock());
        verify(stockDecreaseEventRepository, times(1)).saveAndFlush(any(StockDecreaseEvent.class));
        verify(catalogRepository, times(1)).decreaseStockIfAvailable(catalogId, 3);
        verify(catalogRepository, times(1)).findById(catalogId);
    }

    @Test
    void testDecreaseStockIdempotentWhenDuplicate() {
        UUID requestId = UUID.randomUUID();
        when(stockDecreaseEventRepository.existsByRequestId(requestId)).thenReturn(true);
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));

        Catalog result = catalogService.decreaseStock(catalogId, requestId, 3);

        assertNotNull(result);
        verify(catalogRepository, never()).decreaseStockIfAvailable(any(UUID.class), anyInt());
        verify(stockDecreaseEventRepository, never()).saveAndFlush(any(StockDecreaseEvent.class));
    }

    @Test
    void testDecreaseStockBadRequestWhenQuantityInvalid() {
        UUID requestId = UUID.randomUUID();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            catalogService.decreaseStock(catalogId, requestId, 0);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(catalogRepository, never()).decreaseStockIfAvailable(any(UUID.class), anyInt());
        verify(catalogRepository, never()).findById(any(UUID.class));
        verify(catalogRepository, never()).save(any(Catalog.class));
    }

    @Test
    void testDecreaseStockNotFound() {
        UUID requestId = UUID.randomUUID();
        when(stockDecreaseEventRepository.existsByRequestId(requestId)).thenReturn(false);
        when(stockDecreaseEventRepository.saveAndFlush(any(StockDecreaseEvent.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(catalogRepository.decreaseStockIfAvailable(catalogId, 2)).thenReturn(0);
        when(catalogRepository.existsById(catalogId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            catalogService.decreaseStock(catalogId, requestId, 2);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(catalogRepository, times(1)).decreaseStockIfAvailable(catalogId, 2);
        verify(catalogRepository, times(1)).existsById(catalogId);
        verify(catalogRepository, never()).findById(catalogId);
    }

    @Test
    void testDecreaseStockConflictWhenInsufficientStock() {
        UUID requestId = UUID.randomUUID();
        when(stockDecreaseEventRepository.existsByRequestId(requestId)).thenReturn(false);
        when(stockDecreaseEventRepository.saveAndFlush(any(StockDecreaseEvent.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(catalogRepository.decreaseStockIfAvailable(catalogId, 999)).thenReturn(0);
        when(catalogRepository.existsById(catalogId)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            catalogService.decreaseStock(catalogId, requestId, 999);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(catalogRepository, times(1)).decreaseStockIfAvailable(catalogId, 999);
        verify(catalogRepository, times(1)).existsById(catalogId);
        verify(catalogRepository, never()).findById(catalogId);
    }

    @Test
    void testApplyProductRatingSuccess() {
        ProductRatingUpdateRequest request = new ProductRatingUpdateRequest();
        request.setOrderId(UUID.randomUUID());
        request.setBuyerId(titiperUser.getId());
        request.setProductRating(5);

        when(catalogRatingEventRepository.existsByOrderId(request.getOrderId())).thenReturn(false);
        when(catalogRepository.existsById(catalogId)).thenReturn(true);
        when(catalogRatingEventRepository.save(any(CatalogRatingEvent.class)))
            .thenAnswer(invocation -> {
                Object arg = invocation.getArgument(0);
                return arg;
            });
        when(catalogRepository.applyProductRating(catalogId, 5)).thenReturn(1);
        testCatalog.setRatingAverage(5.0);
        testCatalog.setRatingCount(1);
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));

        var response = catalogService.applyProductRating(catalogId, request, titiperUser);

        assertTrue(response.isApplied());
        assertEquals(5.0, response.getRatingAverage());
        assertEquals(1, response.getRatingCount());
        verify(catalogRepository, times(1)).applyProductRating(catalogId, 5);
    }

    @Test
    void testApplyProductRatingDuplicateOrderId() {
        ProductRatingUpdateRequest request = new ProductRatingUpdateRequest();
        request.setOrderId(UUID.randomUUID());
        request.setBuyerId(titiperUser.getId());
        request.setProductRating(4);

        when(catalogRatingEventRepository.existsByOrderId(request.getOrderId())).thenReturn(true);
        testCatalog.setRatingAverage(4.2);
        testCatalog.setRatingCount(10);
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(testCatalog));

        var response = catalogService.applyProductRating(catalogId, request, titiperUser);

        assertFalse(response.isApplied());
        assertEquals(4.2, response.getRatingAverage());
        assertEquals(10, response.getRatingCount());
        verify(catalogRepository, never()).applyProductRating(any(UUID.class), anyInt());
    }

    @Test
    void testApplyProductRatingForbiddenWhenRoleNotTitiper() {
        ProductRatingUpdateRequest request = new ProductRatingUpdateRequest();
        request.setOrderId(UUID.randomUUID());
        request.setBuyerId(testUser.getId());
        request.setProductRating(5);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            catalogService.applyProductRating(catalogId, request, testUser);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }
}
