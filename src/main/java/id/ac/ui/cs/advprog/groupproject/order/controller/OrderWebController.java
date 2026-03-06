package id.ac.ui.cs.advprog.groupproject.order.controller;

import id.ac.ui.cs.advprog.groupproject.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/order")
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    public String getAllOrders(Model model) {
        model.addAttribute("orders", orderService.findAll());
        return "order/list";
    }
}