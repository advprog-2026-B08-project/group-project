package id.ac.ui.cs.advprog.groupproject.catalog.controller;

import id.ac.ui.cs.advprog.groupproject.catalog.command.CreateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.command.UpdateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.CatalogDto;
import id.ac.ui.cs.advprog.groupproject.catalog.mapper.CatalogMapper;
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

import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
<<<<<<< HEAD
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
=======
import id.ac.ui.cs.advprog.groupproject.model.User;
import id.ac.ui.cs.advprog.groupproject.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.service.CatalogImageService;
>>>>>>> 5fb8bbb3e1f8684f57e642835df2942f9239c7ea
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

    private User testUser;
    private User customerUser;
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
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);

        when(catalogService.getAllCatalogs()).thenReturn(catalogs);
        when(catalogMapper.toDtoList(catalogs)).thenReturn(List.of(testCatalogDto));

        mockMvc.perform(get("/catalog")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/html/catalog"))
                .andExpect(model().attributeExists("catalogs"));

        verify(catalogService, times(1)).getAllCatalogs();
        verify(catalogMapper, times(1)).toDtoList(catalogs);
    }

    @Test
    void testUserCatalogPage() throws Exception {
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(catalogService.getCatalogsByUserId(userId)).thenReturn(catalogs);
        when(catalogMapper.toDtoList(catalogs)).thenReturn(List.of(testCatalogDto));

        mockMvc.perform(get("/catalog/" + userId)
                .with(user("testuser")))
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
        when(catalogMapper.toDtoList(catalogs)).thenReturn(List.of(testCatalogDto));

        mockMvc.perform(get("/catalog/my")
                .with(user("testuser")))
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
                .with(user("testuser")))
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
        when(catalogMapper.toUpdateCommand(any(CatalogDto.class))).thenReturn(
            new UpdateCatalogCommand(
                testCatalogDto.getName(),
                testCatalogDto.getDescription(),
                testCatalogDto.getImageUrl(),
                testCatalogDto.getPrice(),
                testCatalogDto.getStock(),
                testCatalogDto.getOriginLocation(),
                testCatalogDto.getTravelDate()
            )
        );
        when(catalogService.updateCatalog(any(UUID.class), any(UpdateCatalogCommand.class), any(User.class)))
                .thenReturn(testCatalog);

        mockMvc.perform(post("/catalog/edit")
                .with(user("testuser"))
                .with(csrf())
            .flashAttr("catalog", testCatalogDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/my"));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogMapper, times(1)).toUpdateCommand(any(CatalogDto.class));
        verify(catalogService, times(1)).updateCatalog(any(UUID.class), any(UpdateCatalogCommand.class), any(User.class));
    }

    @Test
    void testAddCatalogPage() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/catalog/add")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/html/addCatalog"))
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
        when(catalogMapper.toCreateCommand(any(CatalogDto.class))).thenReturn(
            new CreateCatalogCommand(
                testCatalogDto.getName(),
                testCatalogDto.getDescription(),
                testCatalogDto.getImageUrl(),
                testCatalogDto.getPrice(),
                testCatalogDto.getStock(),
                testCatalogDto.getOriginLocation(),
                testCatalogDto.getTravelDate()
            )
        );
        when(catalogService.createCatalog(any(CreateCatalogCommand.class), any(User.class))).thenReturn(testCatalog);

        mockMvc.perform(post("/catalog/add")
                .with(user("testuser"))
                .with(csrf())
            .flashAttr("catalog", testCatalogDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/my"));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(catalogMapper, times(1)).toCreateCommand(any(CatalogDto.class));
        verify(catalogService, times(1)).createCatalog(any(CreateCatalogCommand.class), any(User.class));
    }

    @Test
    void testCreateCatalogPostForbidden() throws Exception {
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customerUser));

        mockMvc.perform(post("/catalog/add")
                .with(user("customer"))
                .with(csrf())
            .flashAttr("catalog", testCatalogDto))
                .andExpect(status().isForbidden());

        verify(userRepository, times(1)).findByUsername("customer");
    }
}
