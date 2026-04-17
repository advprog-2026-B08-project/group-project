package id.ac.ui.cs.advprog.groupproject.order.adapter;

import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import id.ac.ui.cs.advprog.groupproject.order.dto.ProductSnapshot;
import id.ac.ui.cs.advprog.groupproject.order.port.StockPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CatalogStockAdapter implements StockPort {

    private final CatalogRepository catalogRepository;

    public CatalogStockAdapter(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Override
    public ProductSnapshot reserveStock(UUID productId, int quantity) {
        Catalog catalog = catalogRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (catalog.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + catalog.getName());
        }

        catalog.setStock(catalog.getStock() - quantity);
        catalogRepository.save(catalog);

        return new ProductSnapshot(
                catalog.getJastiper().getId(),
                BigDecimal.valueOf(catalog.getPrice()),
                catalog.getName()
        );
    }

    @Override
    public void releaseStock(UUID productId, int quantity) {
        Catalog catalog = catalogRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        catalog.setStock(catalog.getStock() + quantity);
        catalogRepository.save(catalog);
    }
}
