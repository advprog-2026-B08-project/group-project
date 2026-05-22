package id.ac.ui.cs.advprog.groupproject.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import id.ac.ui.cs.advprog.groupproject.order.dto.OrderDisplayDto;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import id.ac.ui.cs.advprog.groupproject.order.repository.StatusHistoryRepository;
import id.ac.ui.cs.advprog.groupproject.order.service.OrderService;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.WalletResponse;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Functional / web-layer test untuk OrderWebController.
 * Menggunakan MockMvc standalone (tanpa Spring context penuh) sehingga cepat.
 */
@ExtendWith(MockitoExtension.class)
class OrderWebControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private CatalogRepository catalogRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private OrderWebController controller;

    private MockMvc mockMvc;

    private UUID userId;
    private UUID jastiperId;
    private UUID productId;
    private UUID orderId;
    private User titiper;
    private User jastiper;
    private User admin;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        userId = UUID.randomUUID();
        jastiperId = UUID.randomUUID();
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        titiper = new User();
        titiper.setId(userId);
        titiper.setUsername("titiper1");
        titiper.setRole("ROLE_TITIPER");

        jastiper = new User();
        jastiper.setId(jastiperId);
        jastiper.setUsername("jastiper1");
        jastiper.setRole("ROLE_JASTIPER");

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setUsername("admin1");
        admin.setRole("ROLE_ADMIN");

        sampleOrder = new Order();
        sampleOrder.setId(orderId);
        sampleOrder.setBuyerId(userId);
        sampleOrder.setJastiperId(jastiperId);
        sampleOrder.setProductId(productId);
        sampleOrder.setQuantity(2);
        sampleOrder.setShippingAddress("Jl. Test No. 1");
        sampleOrder.setTotalPrice(BigDecimal.valueOf(200000));
        sampleOrder.setStatus(OrderStatus.PAID);
    }

    // ─── /order/list ──────────────────────────────────────────────────────────

    @Test
    void getAllOrders_noPrincipal_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/order/list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getAllOrders_asTitiper_returnsListView() throws Exception {
        when(userRepository.findByUsername("titiper1")).thenReturn(Optional.of(titiper));
        when(orderService.findByBuyerId(userId)).thenReturn(List.of(sampleOrder));
        when(orderService.findByJastiperId(userId)).thenReturn(List.of());
        when(orderService.findBuyerActiveOrders(userId)).thenReturn(List.of(sampleOrder));
        when(orderService.findBuyerCompletedOrders(userId)).thenReturn(List.of());
        when(catalogRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(titiper));
        when(userRepository.findById(jastiperId)).thenReturn(Optional.of(jastiper));

        mockMvc.perform(get("/order/list").principal(() -> "titiper1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/list"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("activeOrders"))
                .andExpect(model().attributeExists("completedOrders"))
                .andExpect(model().attribute("isAdmin", false))
                .andExpect(model().attribute("isJastiper", false));
    }

    @Test
    void getAllOrders_asJastiper_returnsListViewWithTabs() throws Exception {
        when(userRepository.findByUsername("jastiper1")).thenReturn(Optional.of(jastiper));
        when(orderService.findByBuyerId(jastiperId)).thenReturn(List.of());
        when(orderService.findByJastiperId(jastiperId)).thenReturn(List.of(sampleOrder));
        when(orderService.findJastiperTodoOrders(jastiperId)).thenReturn(List.of(sampleOrder));
        when(orderService.findJastiperCompletedOrders(jastiperId)).thenReturn(List.of());
        when(catalogRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(titiper));
        when(userRepository.findById(jastiperId)).thenReturn(Optional.of(jastiper));

        mockMvc.perform(get("/order/list").principal(() -> "jastiper1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/list"))
                .andExpect(model().attributeExists("todoOrders"))
                .andExpect(model().attributeExists("doneOrders"))
                .andExpect(model().attribute("isJastiper", true));
    }

    @Test
    void getAllOrders_asAdmin_showsAllOrders() throws Exception {
        when(userRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));
        when(orderService.findAll()).thenReturn(List.of(sampleOrder));
        when(catalogRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(titiper));
        when(userRepository.findById(jastiperId)).thenReturn(Optional.of(jastiper));

        mockMvc.perform(get("/order/list").principal(() -> "admin1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/list"))
                .andExpect(model().attribute("isAdmin", true));
    }

    // ─── /order/{id} ──────────────────────────────────────────────────────────

    @Test
    void getOrderDetail_found_returnsDetailView() throws Exception {
        when(orderService.findById(orderId)).thenReturn(Optional.of(sampleOrder));
        when(statusHistoryRepository.findByOrderIdOrderByTimestampAsc(orderId)).thenReturn(List.of());
        when(catalogRepository.findById(productId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(titiper));
        when(userRepository.findById(jastiperId)).thenReturn(Optional.of(jastiper));
        when(userRepository.findByUsername("titiper1")).thenReturn(Optional.of(titiper));

        mockMvc.perform(get("/order/{id}", orderId).principal(() -> "titiper1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/detail"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("statusHistory"));
    }

    @Test
    void getOrderDetail_noPrincipal_returnsDetailView() throws Exception {
        when(orderService.findById(orderId)).thenReturn(Optional.of(sampleOrder));
        when(statusHistoryRepository.findByOrderIdOrderByTimestampAsc(orderId)).thenReturn(List.of());
        when(catalogRepository.findById(productId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(titiper));
        when(userRepository.findById(jastiperId)).thenReturn(Optional.of(jastiper));

        mockMvc.perform(get("/order/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("order/detail"));
    }

    // ─── /order/checkout/{productId} ──────────────────────────────────────────

    @Test
    void checkoutForm_noPrincipal_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/order/checkout/{productId}", productId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void checkoutForm_withPrincipal_returnsCheckoutView() throws Exception {
        Catalog product = new Catalog();
        product.setId(productId);
        product.setName("Baju Bola");
        product.setPrice(50000.0);
        product.setStock(10);
        product.setJastiper(jastiper);

        WalletResponse walletResponse = new WalletResponse(userId, BigDecimal.valueOf(500000), null);

        when(userRepository.findByUsername("titiper1")).thenReturn(Optional.of(titiper));
        when(catalogRepository.findById(productId)).thenReturn(Optional.of(product));
        when(walletService.getBalance(userId)).thenReturn(walletResponse);

        mockMvc.perform(get("/order/checkout/{productId}", productId).principal(() -> "titiper1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/checkout"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("walletBalance"));
    }

    @Test
    void getAllOrders_catalogEnrichment_withProductInfo() throws Exception {
        Catalog product = new Catalog();
        product.setId(productId);
        product.setName("Baju Bola");
        product.setImageUrl("https://example.com/img.jpg");
        product.setJastiper(jastiper);

        when(userRepository.findByUsername("titiper1")).thenReturn(Optional.of(titiper));
        when(orderService.findByBuyerId(userId)).thenReturn(List.of(sampleOrder));
        when(orderService.findByJastiperId(userId)).thenReturn(List.of());
        when(orderService.findBuyerActiveOrders(userId)).thenReturn(List.of());
        when(orderService.findBuyerCompletedOrders(userId)).thenReturn(List.of());
        when(catalogRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.of(titiper));
        when(userRepository.findById(jastiperId)).thenReturn(Optional.of(jastiper));

        mockMvc.perform(get("/order/list").principal(() -> "titiper1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/list"));
    }

    @Test
    void getAllOrders_userNotFound_returns401() throws Exception {
        // lambda$getAllOrders$0: orElseThrow when user not found
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/order/list").principal(() -> "unknown"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getOrderDetail_orderNotFound_returns404() throws Exception {
        // order not found → ResponseStatusException 404
        when(orderService.findById(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/order/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkoutForm_userNotFound_returns401() throws Exception {
        // lambda$checkoutForm$3: orElseThrow when user not found
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/order/checkout/{productId}", productId).principal(() -> "unknown"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void checkoutForm_productNotFound_returns404() throws Exception {
        // lambda$checkoutForm$4: orElseThrow when product not found
        when(userRepository.findByUsername("titiper1")).thenReturn(Optional.of(titiper));
        when(catalogRepository.findById(productId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/order/checkout/{productId}", productId).principal(() -> "titiper1"))
                .andExpect(status().isNotFound());
    }
}
