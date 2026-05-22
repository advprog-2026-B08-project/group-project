package id.ac.ui.cs.advprog.groupproject.order.adapter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.service.CatalogService;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateRequest;

@ExtendWith(MockitoExtension.class)
class CatalogRatingAdapterTest {

    @Mock
    private CatalogService catalogService;

    @Mock
    private UserRepository userRepository;

    private CatalogRatingAdapter catalogRatingAdapter;

    private Order order;
    private UUID orderId;
    private UUID buyerId;
    private UUID productId;
    private User buyer;

    @BeforeEach
    void setUp() {
        catalogRatingAdapter = new CatalogRatingAdapter(catalogService, userRepository);
        
        orderId = UUID.randomUUID();
        buyerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        
        order = new Order();
        order.setId(orderId);
        order.setBuyerId(buyerId);
        order.setProductId(productId);
        
        buyer = new User();
        buyer.setId(buyerId);
    }

    @Test
    void propagateProductRating_success() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        
        catalogRatingAdapter.propagateProductRating(order, 5);
        
        verify(catalogService).applyProductRating(eq(productId), any(ProductRatingUpdateRequest.class), eq(buyer));
    }

    @Test
    void propagateProductRating_buyerNotFound() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.empty());
        
        assertDoesNotThrow(() -> catalogRatingAdapter.propagateProductRating(order, 5));
        
        verify(catalogService, never()).applyProductRating(any(), any(), any());
    }

    @Test
    void propagateProductRating_exceptionCaught() {
        when(userRepository.findById(buyerId)).thenThrow(new RuntimeException("DB error"));
        
        assertDoesNotThrow(() -> catalogRatingAdapter.propagateProductRating(order, 5));
    }

    @Test
    void propagateProductRating_catalogServiceThrows_caught() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        doThrow(new RuntimeException("Catalog down")).when(catalogService).applyProductRating(any(), any(), any());
        
        assertDoesNotThrow(() -> catalogRatingAdapter.propagateProductRating(order, 5));
    }
}
