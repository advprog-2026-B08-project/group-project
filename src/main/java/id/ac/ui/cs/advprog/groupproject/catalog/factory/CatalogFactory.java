package id.ac.ui.cs.advprog.groupproject.catalog.factory;

import id.ac.ui.cs.advprog.groupproject.catalog.command.CreateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.command.UpdateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.model.User;
import org.springframework.stereotype.Component;

@Component
public class CatalogFactory {

  public Catalog create(CreateCatalogCommand command, User jastiper) {
    Catalog catalog = new Catalog();
    catalog.setName(command.getName());
    catalog.setDescription(command.getDescription());
    catalog.setImageUrl(command.getImageUrl());
    catalog.setPrice(command.getPrice());
    catalog.setStock(command.getStock());
    catalog.setOriginLocation(command.getOriginLocation());
    catalog.setTravelDate(command.getTravelDate());
    catalog.setJastiper(jastiper);
    return catalog;
  }

  public void applyUpdate(Catalog catalog, UpdateCatalogCommand command) {
    catalog.setName(command.getName());
    catalog.setDescription(command.getDescription());
    catalog.setImageUrl(command.getImageUrl());
    catalog.setPrice(command.getPrice());
    catalog.setStock(command.getStock());
    catalog.setOriginLocation(command.getOriginLocation());
    catalog.setTravelDate(command.getTravelDate());
  }
}
