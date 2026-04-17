package id.ac.ui.cs.advprog.groupproject.catalog.controller;

import id.ac.ui.cs.advprog.groupproject.catalog.command.CreateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.command.UpdateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.CatalogDto;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.DecreaseStockRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.mapper.CatalogMapper;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
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
    private CatalogMapper catalogMapper;

    @Mock
    private Principal principal;

    @InjectMocks
    private CatalogApiController catalogApiController;

    private User testUser;
    private Catalog testCatalog;
    private CatalogDto testCatalogDto;
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

        testCatalogDto = new CatalogDto();
        testCatalogDto.setId(catalogId);
        testCatalogDto.setName("Test Product");
        testCatalogDto.setDescription("Test Description");
        testCatalogDto.setImageUrl("http://example.com/image.jpg");
        testCatalogDto.setPrice(100.0);
        testCatalogDto.setStock(10);
        testCatalogDto.setOriginLocation("Jakarta");
        testCatalogDto.setTravelDate(LocalDate.now().plusDays(7));
    }

    @Test
    void testGetAllMyCatalogs() {
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);
        List<CatalogDto> catalogDtos = new ArrayList<>();
        catalogDtos.add(testCatalogDto);

        when(principal.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogService.findAllCatalogs(any(User.class))).thenReturn(catalogs);
        when(catalogMapper.toDtoList(catalogs)).thenReturn(catalogDtos);

        ResponseEntity<List<CatalogDto>> response = catalogApiController.getAllMyCatalogs(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Product", response.getBody().get(0).getName());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogService, times(1)).findAllCatalogs(any(User.class));
        verify(catalogMapper, times(1)).toDtoList(catalogs);
    }

    @Test
    void testCreateCatalogSuccess() {
        CreateCatalogCommand command = new CreateCatalogCommand(
            testCatalogDto.getName(),
            testCatalogDto.getDescription(),
            testCatalogDto.getImageUrl(),
            testCatalogDto.getPrice(),
            testCatalogDto.getStock(),
            testCatalogDto.getOriginLocation(),
            testCatalogDto.getTravelDate()
        );

        when(principal.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogMapper.toCreateCommand(any(CatalogDto.class))).thenReturn(command);
        when(catalogService.createCatalog(any(CreateCatalogCommand.class), any(User.class))).thenReturn(testCatalog);
        when(catalogMapper.toDto(testCatalog)).thenReturn(testCatalogDto);

        ResponseEntity<CatalogDto> response = catalogApiController.createCatalog(testCatalogDto, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Product", response.getBody().getName());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogMapper, times(1)).toCreateCommand(testCatalogDto);
        verify(catalogService, times(1)).createCatalog(any(CreateCatalogCommand.class), any(User.class));
        verify(catalogMapper, times(1)).toDto(testCatalog);
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
            catalogApiController.createCatalog(testCatalogDto, principal);
        });

        verify(userRepository, times(1)).findByUsername("customer");
        verify(catalogService, never()).createCatalog(any(), any());
    }

    @Test
    void testUpdateCatalogSuccess() {
        UpdateCatalogCommand command = new UpdateCatalogCommand(
            testCatalogDto.getName(),
            testCatalogDto.getDescription(),
            testCatalogDto.getImageUrl(),
            testCatalogDto.getPrice(),
            testCatalogDto.getStock(),
            testCatalogDto.getOriginLocation(),
            testCatalogDto.getTravelDate()
        );

        when(principal.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogMapper.toUpdateCommand(any(CatalogDto.class))).thenReturn(command);
        when(catalogService.updateCatalog(eq(catalogId), any(UpdateCatalogCommand.class), any(User.class)))
                .thenReturn(testCatalog);
        when(catalogMapper.toDto(testCatalog)).thenReturn(testCatalogDto);

        ResponseEntity<CatalogDto> response = catalogApiController.updateCatalog(catalogId, testCatalogDto, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Product", response.getBody().getName());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogMapper, times(1)).toUpdateCommand(testCatalogDto);
        verify(catalogService, times(1)).updateCatalog(eq(catalogId), any(UpdateCatalogCommand.class), any(User.class));
        verify(catalogMapper, times(1)).toDto(testCatalog);
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

    @Test
    void testGetAllMyCatalogsUnauthorizedWhenPrincipalMissing() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            catalogApiController.getAllMyCatalogs(null);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(userRepository);
    }

    @Test
    void testCreateCatalogUnauthorizedWhenPrincipalMissing() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            catalogApiController.createCatalog(testCatalogDto, null);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(catalogService);
    }

    @Test
    void testSearchCatalogs() {
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);
        List<CatalogDto> catalogDtos = new ArrayList<>();
        catalogDtos.add(testCatalogDto);

        when(catalogService.searchCatalogs("Test")).thenReturn(catalogs);
        when(catalogMapper.toDtoList(catalogs)).thenReturn(catalogDtos);

        ResponseEntity<List<CatalogDto>> response = catalogApiController.searchCatalogs("Test", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(catalogService, times(1)).searchCatalogs("Test");
        verify(catalogMapper, times(1)).toDtoList(catalogs);
    }

    @Test
    void testDecreaseStockSuccess() {
        DecreaseStockRequest request = new DecreaseStockRequest();
        request.setQuantity(2);

        Catalog updatedCatalog = new Catalog();
        updatedCatalog.setId(catalogId);
        updatedCatalog.setStock(8);

        CatalogDto updatedDto = new CatalogDto();
        updatedDto.setId(catalogId);
        updatedDto.setStock(8);

        when(catalogService.decreaseStock(catalogId, 2)).thenReturn(updatedCatalog);
        when(catalogMapper.toDto(updatedCatalog)).thenReturn(updatedDto);

        ResponseEntity<CatalogDto> response = catalogApiController.decreaseStock(catalogId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(8, response.getBody().getStock());
        verify(catalogService, times(1)).decreaseStock(catalogId, 2);
        verify(catalogMapper, times(1)).toDto(updatedCatalog);
    }

    @Test
    void testDecreaseStockConflict() {
        DecreaseStockRequest request = new DecreaseStockRequest();
        request.setQuantity(99);

        when(catalogService.decreaseStock(catalogId, 99))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            catalogApiController.decreaseStock(catalogId, request);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(catalogService, times(1)).decreaseStock(catalogId, 99);
    }
}
