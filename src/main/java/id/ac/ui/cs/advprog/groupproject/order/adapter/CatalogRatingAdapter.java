package id.ac.ui.cs.advprog.groupproject.order.adapter;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.service.CatalogService;
import id.ac.ui.cs.advprog.groupproject.order.model.Order;
import id.ac.ui.cs.advprog.groupproject.order.port.ProductRatingPort;
import org.springframework.stereotype.Component;

@Component
public class CatalogRatingAdapter implements ProductRatingPort {

    private final CatalogService catalogService;
    private final UserRepository userRepository;

    public CatalogRatingAdapter(CatalogService catalogService, UserRepository userRepository) {
        this.catalogService = catalogService;
        this.userRepository = userRepository;
    }

    @Override
    public void propagateProductRating(Order order, int ratingProduk) {
        try {
            User buyer = userRepository.findById(order.getBuyerId())
                    .orElse(null);
            if (buyer != null) {
                ProductRatingUpdateRequest ratingRequest = new ProductRatingUpdateRequest();
                ratingRequest.setOrderId(order.getId());
                ratingRequest.setBuyerId(order.getBuyerId());
                ratingRequest.setProductRating(ratingProduk);
                catalogService.applyProductRating(order.getProductId(), ratingRequest, buyer);
            }
        } catch (Exception e) {
            // Log but don't fail the order rating if catalog update fails
            // The order rating is already saved
        }
    }
}
