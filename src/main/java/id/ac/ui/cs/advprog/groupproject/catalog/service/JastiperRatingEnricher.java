package id.ac.ui.cs.advprog.groupproject.catalog.service;

import id.ac.ui.cs.advprog.groupproject.catalog.dto.CatalogDto;
import id.ac.ui.cs.advprog.groupproject.order.repository.OrderRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JastiperRatingEnricher {

    private static final Logger LOGGER = LoggerFactory.getLogger(JastiperRatingEnricher.class);

    private final OrderRepository orderRepository;

    public JastiperRatingEnricher(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void enrich(List<CatalogDto> catalogs) {
        if (catalogs == null || catalogs.isEmpty()) {
            return;
        }

        Set<UUID> jastiperIds = new HashSet<>();
        for (CatalogDto dto : catalogs) {
            if (dto.getJastiperId() != null) {
                jastiperIds.add(dto.getJastiperId());
            }
        }

        if (jastiperIds.isEmpty()) {
            return;
        }

        Map<UUID, Double> ratingByJastiper = new HashMap<>();
        try {
            for (Object[] row : orderRepository.findAverageJastiperRatings(jastiperIds)) {
                UUID jastiperId = (UUID) row[0];
                Double avg = (Double) row[1];
                ratingByJastiper.put(jastiperId, avg);
            }
        } catch (Exception ex) {
            LOGGER.debug("jastiper_rating_enrichment_failed reason={}", ex.getMessage());
            return;
        }

        for (CatalogDto dto : catalogs) {
            if (dto.getJastiperId() != null) {
                dto.setJastiperRatingAverage(ratingByJastiper.get(dto.getJastiperId()));
            }
        }
    }
}
