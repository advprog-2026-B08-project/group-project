package id.ac.ui.cs.advprog.groupproject.catalog.controller;

import id.ac.ui.cs.advprog.groupproject.catalog.dto.CreateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.UpdateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.CatalogDto;
import id.ac.ui.cs.advprog.groupproject.catalog.mapper.CatalogMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.service.CatalogImageService;
import id.ac.ui.cs.advprog.groupproject.catalog.service.CatalogService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogService catalogService;

    @MockitoBean
    private CatalogImageService catalogImageService;

    @MockitoBean
    private CatalogMapper catalogMapper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private id.ac.ui.cs.advprog.groupproject.catalog.service.JastiperRatingEnricher jastiperRatingEnricher;

    private User testUser;
    private User customerUser;
    private User adminUser;
    private Catalog testCatalog;
    private CatalogDto testCatalogDto;
    private UUID catalogId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setRole("ROLE_JASTIPER");
        testUser.setPassword("password");

        testUser.setEmail("testuser@gmail.com");
        testUser.setStatus("AKTIF");

        customerUser = new User();
        customerUser.setId(UUID.randomUUID());
        customerUser.setUsername("customer");
        customerUser.setRole("ROLE_TITIPER");
        customerUser.setPassword("password");

        customerUser.setEmail("customerUser@gmail.com");
        customerUser.setStatus("AKTIF");

        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setPassword("password");
        adminUser.setEmail("admin@gmail.com");
        adminUser.setStatus("AKTIF");

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
    void testCatalogPage() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/catalog")
            .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/html/catalog"))
                .andExpect(model().attributeExists("currentUserId"));

        // Catalog list is loaded via AJAX, not via the controller.
        verify(catalogService, never()).getAllCatalogs();
        verify(catalogMapper, never()).toDtoList(any());
    }

    @Test
    void testUserCatalogPage() throws Exception {
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(catalogService.getCatalogsByUserId(userId)).thenReturn(catalogs);
        when(catalogMapper.toDtoList(catalogs)).thenReturn(List.of(testCatalogDto));

        mockMvc.perform(get("/catalog/" + userId)
            .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/html/userCatalog"))
                .andExpect(model().attributeExists("catalogs"))
                .andExpect(model().attributeExists("username"));

        verify(userRepository, times(1)).findById(userId);
        verify(catalogService, times(1)).getCatalogsByUserId(userId);
        verify(catalogMapper, times(1)).toDtoList(catalogs);
    }

    @Test
    void testUserCatalogPageNotFound() throws Exception {
        UUID unknownUserId = UUID.randomUUID();
        when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/catalog/" + unknownUserId)
            .with(user(testUser)))
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findById(unknownUserId);
    }

    @Test
    void testMyCatalogPage() throws Exception {
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogService.findAllCatalogs(any(User.class))).thenReturn(catalogs);
        when(catalogMapper.toDtoList(catalogs)).thenReturn(List.of(testCatalogDto));

        mockMvc.perform(get("/catalog/my")
            .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/html/myCatalog"))
                .andExpect(model().attributeExists("catalogs"))
                .andExpect(model().attributeExists("username"));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogService, times(1)).findAllCatalogs(any(User.class));
        verify(catalogMapper, times(1)).toDtoList(catalogs);
    }

    @Test
    void testEditCatalogPage() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogService.getCatalogById(catalogId, testUser)).thenReturn(testCatalog);
        when(catalogMapper.toDto(testCatalog)).thenReturn(testCatalogDto);

        mockMvc.perform(get("/catalog/edit/" + catalogId)
            .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/html/editCatalog"))
                .andExpect(model().attributeExists("catalog"));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogService, times(1)).getCatalogById(catalogId, testUser);
        verify(catalogMapper, times(1)).toDto(testCatalog);
    }

    @Test
    void testUpdateCatalogPost() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogMapper.toUpdateRequest(any(CatalogDto.class))).thenReturn(
            new UpdateCatalogRequest(
                testCatalogDto.getName(),
                testCatalogDto.getDescription(),
                testCatalogDto.getImageUrl(),
                testCatalogDto.getPrice(),
                testCatalogDto.getStock(),
                testCatalogDto.getOriginLocation(),
                testCatalogDto.getTravelDate()
            )
        );
        when(catalogService.updateCatalog(any(UUID.class), any(UpdateCatalogRequest.class), any(User.class)))
                .thenReturn(testCatalog);

        mockMvc.perform(post("/catalog/edit")
                .with(user(testUser))
                .with(csrf())
            .flashAttr("catalog", testCatalogDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/my"));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogMapper, times(1)).toUpdateRequest(any(CatalogDto.class));
        verify(catalogService, times(1)).updateCatalog(any(UUID.class), any(UpdateCatalogRequest.class), any(User.class));
    }

    @Test
    void testAddCatalogPage() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/catalog/add")
            .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/html/addCatalog"))
                .andExpect(model().attributeExists("catalog"));

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testAddCatalogPageForbidden() throws Exception {
        mockMvc.perform(get("/catalog/add")
            .with(user(adminUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homepage"));

        verifyNoInteractions(userRepository);
    }

    @Test
    void testCreateCatalogPost() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogMapper.toCreateRequest(any(CatalogDto.class))).thenReturn(
            new CreateCatalogRequest(
                testCatalogDto.getName(),
                testCatalogDto.getDescription(),
                testCatalogDto.getImageUrl(),
                testCatalogDto.getPrice(),
                testCatalogDto.getStock(),
                testCatalogDto.getOriginLocation(),
                testCatalogDto.getTravelDate()
            )
        );
        when(catalogService.createCatalog(any(CreateCatalogRequest.class), any(User.class))).thenReturn(testCatalog);

        mockMvc.perform(post("/catalog/add")
                .with(user(testUser))
                .with(csrf())
            .flashAttr("catalog", testCatalogDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/my"));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogMapper, times(1)).toCreateRequest(any(CatalogDto.class));
        verify(catalogService, times(1)).createCatalog(any(CreateCatalogRequest.class), any(User.class));
    }

    @Test
    void testCreateCatalogPostForbidden() throws Exception {
        mockMvc.perform(post("/catalog/add")
            .with(user(adminUser))
                .with(csrf())
            .flashAttr("catalog", testCatalogDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homepage"));

        verifyNoInteractions(userRepository);
    }

    @Test
    void testEditCatalogPageForbiddenForNonJastiper() throws Exception {
        mockMvc.perform(get("/catalog/edit/" + catalogId)
            .with(user(adminUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homepage"));

        verifyNoInteractions(userRepository);
    }

    @Test
    void testUpdateCatalogPostForbiddenForNonJastiper() throws Exception {
        mockMvc.perform(post("/catalog/edit")
                .with(user(adminUser))
                .with(csrf())
            .flashAttr("catalog", testCatalogDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homepage"));

        verifyNoInteractions(userRepository);
    }

    @Test
    void testAdminMonitoringPage() throws Exception {
        List<Catalog> catalogs = List.of(testCatalog);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(catalogService.getAllCatalogs()).thenReturn(catalogs);
        when(catalogMapper.toDtoList(catalogs)).thenReturn(List.of(testCatalogDto));

        mockMvc.perform(get("/catalog/admin/monitoring")
                .with(user(adminUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog/html/adminCatalogMonitoring"))
            .andExpect(model().attributeExists("catalogs"));

        verify(userRepository, times(1)).findByUsername("admin");
        verify(catalogService, times(1)).getAllCatalogs();
    }

    @Test
    void testAdminEditCatalogPage() throws Exception {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(catalogService.getCatalogByIdForAdmin(catalogId)).thenReturn(testCatalog);
        when(catalogMapper.toDto(testCatalog)).thenReturn(testCatalogDto);

        mockMvc.perform(get("/catalog/admin/edit/" + catalogId)
                .with(user(adminUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog/html/editCatalog"))
            .andExpect(model().attributeExists("catalog"));

        verify(catalogService, times(1)).getCatalogByIdForAdmin(catalogId);
    }

    @Test
    void testAdminDeleteCatalogPost() throws Exception {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        mockMvc.perform(post("/catalog/admin/delete/" + catalogId)
                .with(user(adminUser))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/catalog/admin/monitoring"));

        verify(catalogService, times(1)).deleteCatalogByAdmin(catalogId);
    }

    // ── Detail Page ─────────────────────────────────────────────────────

    @Test
    void testCatalogDetailPage() throws Exception {
        UUID jastiperId = testUser.getId();
        testCatalogDto.setJastiperId(jastiperId);
        testCatalogDto.setJastiperUsername(testUser.getUsername());

        when(catalogService.getCatalogByIdForAdmin(catalogId)).thenReturn(testCatalog);
        when(catalogMapper.toDto(testCatalog)).thenReturn(testCatalogDto);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/catalog/detail/" + catalogId)
                .with(user(testUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog/html/detailCatalog"))
            .andExpect(model().attributeExists("catalog"))
            .andExpect(model().attributeExists("currentUserId"));

        verify(catalogService, times(1)).getCatalogByIdForAdmin(catalogId);
        verify(catalogMapper, times(1)).toDto(testCatalog);
    }

    // ── Catalog Page (server renders shell only; data is loaded via AJAX) ───

    @Test
    void testUserCatalogPageEnrichesViaEnricher() throws Exception {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(catalogService.getCatalogsByUserId(userId)).thenReturn(List.of(testCatalog));
        when(catalogMapper.toDtoList(any())).thenReturn(List.of(testCatalogDto));

        mockMvc.perform(get("/catalog/" + userId)
                .with(user(testUser)))
            .andExpect(status().isOk());

        verify(jastiperRatingEnricher, times(1)).enrich(any());
    }

    // ── BindingResult validation paths ──────────────────────────────────

    @Test
    void testCreateCatalogPostWithInvalidDataReturnsToForm() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Send DTO with blank name to trigger @NotBlank validation error
        CatalogDto invalidDto = new CatalogDto();
        invalidDto.setName("");
        invalidDto.setPrice(100.0);
        invalidDto.setStock(10);
        invalidDto.setOriginLocation("Jakarta");
        invalidDto.setTravelDate(LocalDate.now().plusDays(7));

        mockMvc.perform(post("/catalog/add")
                .with(user(testUser))
                .with(csrf())
                .flashAttr("catalog", invalidDto))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog/html/addCatalog"));

        verify(catalogService, never()).createCatalog(any(), any());
    }

    @Test
    void testUpdateCatalogPostWithInvalidDataReturnsToForm() throws Exception {
        CatalogDto invalidDto = new CatalogDto();
        invalidDto.setName("");
        invalidDto.setPrice(100.0);
        invalidDto.setStock(10);
        invalidDto.setOriginLocation("Jakarta");
        invalidDto.setTravelDate(LocalDate.now().plusDays(7));

        mockMvc.perform(post("/catalog/edit")
                .with(user(testUser))
                .with(csrf())
                .flashAttr("catalog", invalidDto))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog/html/editCatalog"));

        verify(catalogService, never()).updateCatalog(any(), any(), any());
    }

    @Test
    void testAdminUpdateCatalogPostWithInvalidDataReturnsToForm() throws Exception {
        CatalogDto invalidDto = new CatalogDto();
        invalidDto.setName("");
        invalidDto.setPrice(100.0);
        invalidDto.setStock(10);
        invalidDto.setOriginLocation("Jakarta");
        invalidDto.setTravelDate(LocalDate.now().plusDays(7));

        mockMvc.perform(post("/catalog/admin/edit")
                .with(user(adminUser))
                .with(csrf())
                .flashAttr("catalog", invalidDto))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog/html/editCatalog"));

        verify(catalogService, never()).updateCatalogByAdmin(any(), any());
    }

    // ── File upload paths ───────────────────────────────────────────────

    @Test
    void testCreateCatalogPostWithImageUpload() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogImageService.uploadCatalogImage(any(MultipartFile.class)))
                .thenReturn("http://cloudinary.com/uploaded.jpg");
        when(catalogMapper.toCreateRequest(any(CatalogDto.class))).thenReturn(
            new CreateCatalogRequest(
                testCatalogDto.getName(),
                testCatalogDto.getDescription(),
                "http://cloudinary.com/uploaded.jpg",
                testCatalogDto.getPrice(),
                testCatalogDto.getStock(),
                testCatalogDto.getOriginLocation(),
                testCatalogDto.getTravelDate()));
        when(catalogService.createCatalog(any(CreateCatalogRequest.class), any(User.class)))
                .thenReturn(testCatalog);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        mockMvc.perform(multipart("/catalog/add")
                .file(file)
                .with(user(testUser))
                .with(csrf())
                .flashAttr("catalog", testCatalogDto))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/catalog/my"));

        verify(catalogImageService, times(1)).uploadCatalogImage(any(MultipartFile.class));
    }

    @Test
    void testUpdateCatalogPostWithImageUpload() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogImageService.uploadCatalogImage(any(MultipartFile.class)))
                .thenReturn("http://cloudinary.com/updated.jpg");
        when(catalogMapper.toUpdateRequest(any(CatalogDto.class))).thenReturn(
            new UpdateCatalogRequest(
                testCatalogDto.getName(),
                testCatalogDto.getDescription(),
                "http://cloudinary.com/updated.jpg",
                testCatalogDto.getPrice(),
                testCatalogDto.getStock(),
                testCatalogDto.getOriginLocation(),
                testCatalogDto.getTravelDate()));
        when(catalogService.updateCatalog(any(UUID.class), any(UpdateCatalogRequest.class), any(User.class)))
                .thenReturn(testCatalog);

        MockMultipartFile file = new MockMultipartFile(
                "file", "updated.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        mockMvc.perform(multipart("/catalog/edit")
                .file(file)
                .with(user(testUser))
                .with(csrf())
                .flashAttr("catalog", testCatalogDto))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/catalog/my"));

        verify(catalogImageService, times(1)).uploadCatalogImage(any(MultipartFile.class));
    }

    // ── Admin update + admin authorization ──────────────────────────────

    @Test
    void testAdminUpdateCatalogPost() throws Exception {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(catalogMapper.toUpdateRequest(any(CatalogDto.class))).thenReturn(
            new UpdateCatalogRequest(
                testCatalogDto.getName(),
                testCatalogDto.getDescription(),
                testCatalogDto.getImageUrl(),
                testCatalogDto.getPrice(),
                testCatalogDto.getStock(),
                testCatalogDto.getOriginLocation(),
                testCatalogDto.getTravelDate()));
        when(catalogService.updateCatalogByAdmin(any(UUID.class), any(UpdateCatalogRequest.class)))
                .thenReturn(testCatalog);

        mockMvc.perform(post("/catalog/admin/edit")
                .with(user(adminUser))
                .with(csrf())
                .flashAttr("catalog", testCatalogDto))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/catalog/admin/monitoring"));

        verify(catalogService, times(1))
                .updateCatalogByAdmin(any(UUID.class), any(UpdateCatalogRequest.class));
    }

    @Test
    void testAdminUpdateCatalogPostForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(post("/catalog/admin/edit")
                .with(user(testUser))
                .with(csrf())
                .flashAttr("catalog", testCatalogDto))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/homepage"));

        verify(catalogService, never()).updateCatalogByAdmin(any(), any());
    }

    @Test
    void testAdminEditCatalogPageForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/catalog/admin/edit/" + catalogId)
                .with(user(testUser)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/homepage"));

        verify(catalogService, never()).getCatalogByIdForAdmin(any());
    }

    @Test
    void testAdminMonitoringPageForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/catalog/admin/monitoring")
                .with(user(testUser)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/homepage"));

        verify(catalogService, never()).getAllCatalogs();
    }

    @Test
    void testAdminDeleteCatalogPostForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(post("/catalog/admin/delete/" + catalogId)
                .with(user(testUser))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/homepage"));

        verify(catalogService, never()).deleteCatalogByAdmin(any());
    }

    // ── Add/Edit page forbidden for titiper ─────────────────────────────

    @Test
    void testAddCatalogPageForbiddenForTitiper() throws Exception {
        mockMvc.perform(get("/catalog/add")
                .with(user(customerUser)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/homepage"));

        verifyNoInteractions(userRepository);
    }

    @Test
    void testEditCatalogPageForbiddenForTitiper() throws Exception {
        mockMvc.perform(get("/catalog/edit/" + catalogId)
                .with(user(customerUser)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/homepage"));

        verifyNoInteractions(userRepository);
    }
}
