package id.ac.ui.cs.advprog.groupproject.catalog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.groupproject.catalog.dto.CatalogDto;
import id.ac.ui.cs.advprog.groupproject.order.repository.OrderRepository;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JastiperRatingEnricherTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private JastiperRatingEnricher enricher;

    private CatalogDto dtoWithJastiper(UUID jastiperId) {
        CatalogDto dto = new CatalogDto();
        dto.setId(UUID.randomUUID());
        dto.setJastiperId(jastiperId);
        return dto;
    }

    @Test
    void enrich_emptyList_doesNothing() {
        enricher.enrich(Collections.emptyList());
        verify(orderRepository, never()).findAverageJastiperRatings(any());
    }

    @Test
    void enrich_nullList_doesNothing() {
        enricher.enrich(null);
        verify(orderRepository, never()).findAverageJastiperRatings(any());
    }

    @Test
    void enrich_skipsCatalogsWithoutJastiperId() {
        CatalogDto dto = new CatalogDto();
        dto.setId(UUID.randomUUID());
        dto.setJastiperId(null);

        enricher.enrich(List.of(dto));

        verify(orderRepository, never()).findAverageJastiperRatings(any());
        assertNull(dto.getJastiperRatingAverage());
    }

    @Test
    void enrich_singleBatchQueryForMultipleCatalogs() {
        UUID jastiperA = UUID.randomUUID();
        UUID jastiperB = UUID.randomUUID();
        CatalogDto dto1 = dtoWithJastiper(jastiperA);
        CatalogDto dto2 = dtoWithJastiper(jastiperB);
        CatalogDto dto3 = dtoWithJastiper(jastiperA);  // duplicate jastiper

        when(orderRepository.findAverageJastiperRatings(any()))
                .thenReturn(List.of(
                        new Object[]{jastiperA, 4.5},
                        new Object[]{jastiperB, 3.7}));

        enricher.enrich(List.of(dto1, dto2, dto3));

        verify(orderRepository, times(1)).findAverageJastiperRatings(any());

        assertEquals(4.5, dto1.getJastiperRatingAverage());
        assertEquals(3.7, dto2.getJastiperRatingAverage());
        assertEquals(4.5, dto3.getJastiperRatingAverage());
    }

    @Test
    void enrich_handlesRepositoryException() {
        CatalogDto dto = dtoWithJastiper(UUID.randomUUID());

        when(orderRepository.findAverageJastiperRatings(any()))
                .thenThrow(new RuntimeException("DB unavailable"));

        enricher.enrich(List.of(dto));

        assertNull(dto.getJastiperRatingAverage());
    }

    @Test
    void enrich_jastiperWithNoRatings_leavesNull() {
        UUID jastiperId = UUID.randomUUID();
        CatalogDto dto = dtoWithJastiper(jastiperId);

        when(orderRepository.findAverageJastiperRatings(any()))
                .thenReturn(Collections.emptyList());

        enricher.enrich(List.of(dto));

        assertNull(dto.getJastiperRatingAverage());
    }
}
