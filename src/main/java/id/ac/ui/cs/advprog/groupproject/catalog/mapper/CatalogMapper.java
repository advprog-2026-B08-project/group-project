package id.ac.ui.cs.advprog.groupproject.catalog.mapper;

import id.ac.ui.cs.advprog.groupproject.catalog.dto.CatalogDto;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.CreateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.UpdateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CatalogMapper {

  public CatalogDto toDto(Catalog catalog) {
    CatalogDto dto = new CatalogDto();
    dto.setId(catalog.getId());
    if (catalog.getJastiper() != null) {
      dto.setJastiperId(catalog.getJastiper().getId());
      dto.setJastiperUsername(catalog.getJastiper().getUsername());
      dto.setJastiperSuccessRate(catalog.getJastiper().getSuccessRate());
    }
    dto.setName(catalog.getName());
    dto.setDescription(catalog.getDescription());
    dto.setImageUrl(catalog.getImageUrl());
    dto.setPrice(catalog.getPrice());
    dto.setStock(catalog.getStock());
    dto.setRatingAverage(catalog.getRatingAverage());
    dto.setRatingCount(catalog.getRatingCount());
    dto.setOriginLocation(catalog.getOriginLocation());
    dto.setTravelDate(catalog.getTravelDate());
    return dto;
  }

  public List<CatalogDto> toDtoList(List<Catalog> catalogs) {
    return catalogs.stream().map(this::toDto).toList();
  }

  public CreateCatalogRequest toCreateRequest(CatalogDto dto) {
    return new CreateCatalogRequest(
        dto.getName(),
        dto.getDescription(),
        dto.getImageUrl(),
        dto.getPrice(),
        dto.getStock(),
        dto.getOriginLocation(),
        dto.getTravelDate());
  }

  public UpdateCatalogRequest toUpdateRequest(CatalogDto dto) {
    return new UpdateCatalogRequest(
        dto.getName(),
        dto.getDescription(),
        dto.getImageUrl(),
        dto.getPrice(),
        dto.getStock(),
        dto.getOriginLocation(),
        dto.getTravelDate());
  }
}
