package id.ac.ui.cs.advprog.groupproject.order.controller;

import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.repository.StatusHistoryRepository;
import id.ac.ui.cs.advprog.groupproject.order.service.OrderService;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
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

    @GetMapping("/list")
    public String getAllOrders(Model model, Principal principal) {
        model.addAttribute("orders", orderService.findAll());
        if (principal != null) {
            userRepository.findByUsername(principal.getName())
                .ifPresent(user -> model.addAttribute("currentUserId", user.getId().toString()));
        }
        return "order/list";
    }

    @GetMapping("/{id}")
    public String getOrderDetail(@PathVariable UUID id, Model model, Principal principal) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        model.addAttribute("order", order);
        model.addAttribute("statusHistory", statusHistoryRepository.findByOrderIdOrderByTimestampAsc(id));
        if (principal != null) {
            userRepository.findByUsername(principal.getName())
                .ifPresent(user -> model.addAttribute("currentUserId", user.getId().toString()));
        }
        return "order/detail";
    }
}