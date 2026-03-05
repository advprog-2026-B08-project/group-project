package id.ac.ui.cs.advprog.groupproject.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.groupproject.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.model.User;
import id.ac.ui.cs.advprog.groupproject.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.service.CatalogService;

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
    private UserRepository userRepository;

    private User testUser;
    private User customerUser;
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

        customerUser = new User();
        customerUser.setId(UUID.randomUUID());
        customerUser.setUsername("customer");
        customerUser.setRole("CUSTOMER");

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
    void testCatalogPage() throws Exception {
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);

        when(catalogService.getAllCatalogs()).thenReturn(catalogs);

        mockMvc.perform(get("/catalog")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/catalog"))
                .andExpect(model().attributeExists("catalogs"));

        verify(catalogService, times(1)).getAllCatalogs();
    }

    @Test
    void testUserCatalogPage() throws Exception {
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(catalogService.getCatalogsByUserId(userId)).thenReturn(catalogs);

        mockMvc.perform(get("/catalog/" + userId)
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/userCatalog"))
                .andExpect(model().attributeExists("catalogs"))
                .andExpect(model().attributeExists("username"));

        verify(userRepository, times(1)).findById(userId);
        verify(catalogService, times(1)).getCatalogsByUserId(userId);
    }

    @Test
    void testUserCatalogPageNotFound() throws Exception {
        UUID unknownUserId = UUID.randomUUID();
        when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/catalog/" + unknownUserId)
                .with(user("testuser")))
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findById(unknownUserId);
    }

    @Test
    void testMyCatalogPage() throws Exception {
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogService.findAllCatalogs(any(User.class))).thenReturn(catalogs);

        mockMvc.perform(get("/catalog/my")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/myCatalog"))
                .andExpect(model().attributeExists("catalogs"))
                .andExpect(model().attributeExists("username"));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogService, times(1)).findAllCatalogs(any(User.class));
    }

    @Test
    void testEditCatalogPage() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogService.getCatalogById(catalogId, testUser)).thenReturn(testCatalog);

        mockMvc.perform(get("/catalog/edit/" + catalogId)
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/editCatalog"))
                .andExpect(model().attributeExists("catalog"));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogService, times(1)).getCatalogById(catalogId, testUser);
    }

    @Test
    void testUpdateCatalogPost() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogService.updateCatalog(any(UUID.class), any(Catalog.class), any(User.class)))
                .thenReturn(testCatalog);

        mockMvc.perform(post("/catalog/edit")
                .with(user("testuser"))
                .with(csrf())
                .flashAttr("catalog", testCatalog))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/my"));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogService, times(1)).updateCatalog(any(UUID.class), any(Catalog.class), any(User.class));
    }

    @Test
    void testAddCatalogPage() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/catalog/add")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/addCatalog"))
                .andExpect(model().attributeExists("catalog"));

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testAddCatalogPageForbidden() throws Exception {
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customerUser));

        mockMvc.perform(get("/catalog/add")
                .with(user("customer")))
                .andExpect(status().isForbidden());

        verify(userRepository, times(1)).findByUsername("customer");
    }

    @Test
    void testCreateCatalogPost() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(catalogService.createCatalog(any(Catalog.class), any(User.class))).thenReturn(testCatalog);

        mockMvc.perform(post("/catalog/add")
                .with(user("testuser"))
                .with(csrf())
                .flashAttr("catalog", testCatalog))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/my"));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogService, times(1)).createCatalog(any(Catalog.class), any(User.class));
    }

    @Test
    void testCreateCatalogPostForbidden() throws Exception {
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customerUser));

        mockMvc.perform(post("/catalog/add")
                .with(user("customer"))
                .with(csrf())
                .flashAttr("catalog", testCatalog))
                .andExpect(status().isForbidden());

        verify(userRepository, times(1)).findByUsername("customer");
    }
}
