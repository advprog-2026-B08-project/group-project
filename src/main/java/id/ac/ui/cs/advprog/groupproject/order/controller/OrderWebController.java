package id.ac.ui.cs.advprog.groupproject.order.controller;

import id.ac.ui.cs.advprog.groupproject.order.service.OrderService;
import id.ac.ui.cs.advprog.groupproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/order")
public class OrderWebController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/list")
    public String getAllOrders(Model model, Principal principal) {
        model.addAttribute("orders", orderService.findAll());
        if (principal != null) {
            userRepository.findByUsername(principal.getName())
                .ifPresent(user -> model.addAttribute("currentUserId", user.getId().toString()));
        }
        return "order/list";
    }
}