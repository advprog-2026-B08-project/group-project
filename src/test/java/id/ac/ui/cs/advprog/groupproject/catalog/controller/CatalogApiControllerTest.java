package id.ac.ui.cs.advprog.groupproject.catalog.controller;

import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.model.User;
import id.ac.ui.cs.advprog.groupproject.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.service.CatalogService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogApiControllerTest {

    @Mock
    private CatalogService catalogService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Principal principal;

    @InjectMocks
    private CatalogApiController catalogApiController;

    private User testUser;
    private Catalog testCatalog;
    private UUID catalogId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setRole("JASTIPER");

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
    void testGetAllMyCatalogs() {
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);

        when(principal.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogService.findAllCatalogs(any(User.class))).thenReturn(catalogs);

        ResponseEntity<List<Catalog>> response = catalogApiController.getAllMyCatalogs(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Product", response.getBody().get(0).getName());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogService, times(1)).findAllCatalogs(any(User.class));
    }

    @Test
    void testCreateCatalogSuccess() {
        when(principal.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogService.createCatalog(any(Catalog.class), any(User.class))).thenReturn(testCatalog);

        ResponseEntity<Catalog> response = catalogApiController.createCatalog(testCatalog, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Product", response.getBody().getName());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogService, times(1)).createCatalog(any(Catalog.class), any(User.class));
    }

    @Test
    void testCreateCatalogForbidden() {
        User customerUser = new User();
        customerUser.setId(UUID.randomUUID());
        customerUser.setUsername("customer");
        customerUser.setRole("CUSTOMER");

        when(principal.getName()).thenReturn("customer");
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customerUser));

        assertThrows(ResponseStatusException.class, () -> {
            catalogApiController.createCatalog(testCatalog, principal);
        });

        verify(userRepository, times(1)).findByUsername("customer");
        verify(catalogService, never()).createCatalog(any(), any());
    }

    @Test
    void testUpdateCatalogSuccess() {
        when(principal.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogService.updateCatalog(eq(catalogId), any(Catalog.class), any(User.class)))
                .thenReturn(testCatalog);

        ResponseEntity<Catalog> response = catalogApiController.updateCatalog(catalogId, testCatalog, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Product", response.getBody().getName());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogService, times(1)).updateCatalog(eq(catalogId), any(Catalog.class), any(User.class));
    }

    @Test
    void testDeleteCatalogSuccess() {
        when(principal.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(catalogService).deleteCatalog(eq(catalogId), any(User.class));

        ResponseEntity<Void> response = catalogApiController.deleteCatalog(catalogId, principal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogService, times(1)).deleteCatalog(eq(catalogId), any(User.class));
    }

    @Test
    void testGetCurrentUserNotFound() {
        when(principal.getName()).thenReturn("unknownuser");
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            catalogApiController.getAllMyCatalogs(principal);
        });

        verify(userRepository, times(1)).findByUsername("unknownuser");
    }
}
