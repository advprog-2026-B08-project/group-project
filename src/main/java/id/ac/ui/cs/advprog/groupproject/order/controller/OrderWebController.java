package id.ac.ui.cs.advprog.groupproject.order.controller;

import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import id.ac.ui.cs.advprog.groupproject.order.dto.OrderDisplayDto;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.repository.StatusHistoryRepository;
import id.ac.ui.cs.advprog.groupproject.order.service.OrderService;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;
import id.ac.ui.cs.advprog.groupproject.wallet.dto.WalletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/order")
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private WalletService walletService;

    @GetMapping("/list")
    public String getAllOrders(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        UUID userId = currentUser.getId();
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole());
        boolean isJastiper = "ROLE_JASTIPER".equalsIgnoreCase(currentUser.getRole());

        List<Order> orders;
        if (isAdmin) {
            orders = orderService.findAll();
        } else {
            LinkedHashSet<Order> userOrders = new LinkedHashSet<>();
            userOrders.addAll(orderService.findByBuyerId(userId));
            userOrders.addAll(orderService.findByJastiperId(userId));
            orders = new ArrayList<>(userOrders);
        }

        List<OrderDisplayDto> displayOrders = enrichOrders(orders);

        // Role-specific tabs
        if (isJastiper) {
            model.addAttribute("todoOrders", enrichOrders(orderService.findJastiperTodoOrders(userId)));
            model.addAttribute("doneOrders", enrichOrders(orderService.findJastiperCompletedOrders(userId)));
        } else if (!isAdmin) {
            // Titiper
            model.addAttribute("activeOrders", enrichOrders(orderService.findBuyerActiveOrders(userId)));
            model.addAttribute("completedOrders", enrichOrders(orderService.findBuyerCompletedOrders(userId)));
        }

        model.addAttribute("orders", displayOrders);
        model.addAttribute("currentUserId", userId.toString());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isJastiper", isJastiper);
        model.addAttribute("userRole", currentUser.getRole());
        return "order/list";
    }

    @GetMapping("/{id}")
    public String getOrderDetail(@PathVariable UUID id, Model model, Principal principal) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderDisplayDto displayOrder = enrichOrder(order);

        model.addAttribute("order", displayOrder);
        model.addAttribute("statusHistory", statusHistoryRepository.findByOrderIdOrderByTimestampAsc(id));
        if (principal != null) {
            userRepository.findByUsername(principal.getName())
                .ifPresent(user -> {
                    model.addAttribute("currentUserId", user.getId().toString());
                    model.addAttribute("isAdmin", "ROLE_ADMIN".equalsIgnoreCase(user.getRole()));
                });
        }
        return "order/detail";
    }

    @GetMapping("/checkout/{productId}")
    public String checkoutForm(@PathVariable UUID productId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Catalog product = catalogRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // Get wallet balance
        WalletResponse wallet = walletService.getBalance(currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("product", product);
        model.addAttribute("walletBalance", wallet.getBalance());

        return "order/checkout";
    }

    private List<OrderDisplayDto> enrichOrders(List<Order> orders) {
        List<OrderDisplayDto> result = new ArrayList<>();
        for (Order order : orders) {
            result.add(enrichOrder(order));
        }
        return result;
    }

    private OrderDisplayDto enrichOrder(Order order) {
        OrderDisplayDto dto = OrderDisplayDto.from(order);

        // Enrich with product info
        catalogRepository.findById(order.getProductId()).ifPresent(catalog -> {
            dto.setProductName(catalog.getName());
            dto.setProductImageUrl(catalog.getImageUrl());
        });

        // Enrich with user info
        userRepository.findById(order.getBuyerId()).ifPresent(user ->
            dto.setBuyerUsername(user.getUsername()));
        userRepository.findById(order.getJastiperId()).ifPresent(user ->
            dto.setJastiperUsername(user.getUsername()));

        return dto;
    }
}