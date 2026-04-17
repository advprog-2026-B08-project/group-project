package id.ac.ui.cs.advprog.groupproject.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSnapshot(UUID jastiperId, BigDecimal pricePerItem, String productName) {}
