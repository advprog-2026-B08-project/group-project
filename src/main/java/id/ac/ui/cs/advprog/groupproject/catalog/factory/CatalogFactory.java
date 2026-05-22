package id.ac.ui.cs.advprog.groupproject.catalog.factory;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.CreateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.UpdateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import org.springframework.stereotype.Component;

@Component
public class CatalogFactory {

  public Catalog create(CreateCatalogRequest request, User jastiper) {
    Catalog catalog = new Catalog();
    catalog.setName(request.getName());
    catalog.setDescription(request.getDescription());
    catalog.setImageUrl(request.getImageUrl());
    catalog.setPrice(request.getPrice());
    catalog.setStock(request.getStock());
    catalog.setOriginLocation(request.getOriginLocation());
    catalog.setTravelDate(request.getTravelDate());
    catalog.setJastiper(jastiper);
    return catalog;
  }

  public void applyUpdate(Catalog catalog, UpdateCatalogRequest request) {
    catalog.setName(request.getName());
    catalog.setDescription(request.getDescription());
    catalog.setImageUrl(request.getImageUrl());
    catalog.setPrice(request.getPrice());
    catalog.setStock(request.getStock());
    catalog.setOriginLocation(request.getOriginLocation());
    catalog.setTravelDate(request.getTravelDate());
  }
}
