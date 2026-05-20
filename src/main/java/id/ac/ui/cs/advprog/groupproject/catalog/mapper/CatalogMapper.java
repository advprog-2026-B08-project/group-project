package id.ac.ui.cs.advprog.groupproject.catalog.mapper;

import id.ac.ui.cs.advprog.groupproject.catalog.command.CreateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.command.UpdateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.CatalogDto;
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

  public CreateCatalogCommand toCreateCommand(CatalogDto dto) {
    return new CreateCatalogCommand(
        dto.getName(),
        dto.getDescription(),
        dto.getImageUrl(),
        dto.getPrice(),
        dto.getStock(),
        dto.getOriginLocation(),
        dto.getTravelDate());
  }

  public UpdateCatalogCommand toUpdateCommand(CatalogDto dto) {
    return new UpdateCatalogCommand(
        dto.getName(),
        dto.getDescription(),
        dto.getImageUrl(),
        dto.getPrice(),
        dto.getStock(),
        dto.getOriginLocation(),
        dto.getTravelDate());
  }
}
