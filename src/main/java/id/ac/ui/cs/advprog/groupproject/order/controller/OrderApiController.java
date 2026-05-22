package id.ac.ui.cs.advprog.groupproject.order.controller;

import id.ac.ui.cs.advprog.groupproject.order.dto.CheckoutRequest;
import id.ac.ui.cs.advprog.groupproject.order.dto.RatingRequest;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.model.OrderStatus;
import id.ac.ui.cs.advprog.groupproject.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutRequest request) {
        try {
            Order order = orderService.checkout(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable UUID id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status) {
        try {
            Order order = orderService.updateStatus(id, status);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID id) {
        try {
            Order order = orderService.cancelOrder(id);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/rating")
    public ResponseEntity<?> submitRating(
            @PathVariable UUID id,
            @Valid @RequestBody RatingRequest request) {
        try {
            Order order = orderService.submitRating(
                    id,
                    request.getRatingProduk(),
                    request.getRatingJastiper()
            );
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // Titiper: riwayat pesanan aktif
    @GetMapping("/buyer/{buyerId}/active")
    public ResponseEntity<List<Order>> getBuyerActiveOrders(@PathVariable UUID buyerId) {
        return ResponseEntity.ok(orderService.findBuyerActiveOrders(buyerId));
    }

    // Titiper: riwayat pesanan selesai
    @GetMapping("/buyer/{buyerId}/completed")
    public ResponseEntity<List<Order>> getBuyerCompletedOrders(@PathVariable UUID buyerId) {
        return ResponseEntity.ok(orderService.findBuyerCompletedOrders(buyerId));
    }

    // Jastiper: to-do list
    @GetMapping("/jastiper/{jastiperId}/todo")
    public ResponseEntity<List<Order>> getJastiperTodoOrders(@PathVariable UUID jastiperId) {
        return ResponseEntity.ok(orderService.findJastiperTodoOrders(jastiperId));
    }

    // Jastiper: pesanan selesai
    @GetMapping("/jastiper/{jastiperId}/done")
    public ResponseEntity<List<Order>> getJastiperDoneOrders(@PathVariable UUID jastiperId) {
        return ResponseEntity.ok(orderService.findJastiperCompletedOrders(jastiperId));
    }
}