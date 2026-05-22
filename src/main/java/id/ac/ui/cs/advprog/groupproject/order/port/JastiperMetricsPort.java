package id.ac.ui.cs.advprog.groupproject.order.port;

import java.util.UUID;

public interface JastiperMetricsPort {
    void incrementTriedToSell(UUID jastiperId);
    void incrementSuccessfullySold(UUID jastiperId);
}
