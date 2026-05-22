package id.ac.ui.cs.advprog.groupproject.order.port;

import id.ac.ui.cs.advprog.groupproject.order.model.Order;

public interface ProductRatingPort {
    void propagateProductRating(Order order, int ratingProduk);
}
