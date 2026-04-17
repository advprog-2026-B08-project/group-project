package id.ac.ui.cs.advprog.groupproject.order.model;

public enum OrderStatus {
    PAID, PURCHASED, SHIPPED, COMPLETED, CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PAID -> next == PURCHASED || next == CANCELLED;
            case PURCHASED -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}