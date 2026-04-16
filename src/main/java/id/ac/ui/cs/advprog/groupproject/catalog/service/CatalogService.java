package id.ac.ui.cs.advprog.groupproject.catalog.service;

import id.ac.ui.cs.advprog.groupproject.catalog.command.CreateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.command.UpdateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.factory.CatalogFactory;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import id.ac.ui.cs.advprog.groupproject.model.User;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
  private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found";
  private static final String AUTH_FAILED_MESSAGE = "Auth Failed!";

  private final CatalogRepository catalogRepository;
  private final CatalogFactory catalogFactory;

  public CatalogService(CatalogRepository catalogRepository, CatalogFactory catalogFactory) {
    this.catalogRepository = catalogRepository;
    this.catalogFactory = catalogFactory;
  }

  public Catalog createCatalog(CreateCatalogCommand command, User currentUser) {
    if (!currentUser.isJastiper()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Jastiper can create catalog");
    }

    Catalog catalog = catalogFactory.create(command, currentUser);
    return catalogRepository.save(catalog);
  }

  public List<Catalog> findAllCatalogs(User currentUser) {
    return catalogRepository.findByJastiperId(currentUser.getId());
  }

  public List<Catalog> getAllCatalogs() {
    return catalogRepository.findAll();
  }

  public List<Catalog> searchCatalogs(String name, String jastiper) {
    return catalogRepository.searchCatalogs(
        normalizeSearchTerm(name), normalizeSearchTerm(jastiper));
  }

  public List<Catalog> searchCatalogs(String keyword) {
    return catalogRepository.searchCatalogsByKeyword(normalizeSearchTerm(keyword));
  }

  public List<Catalog> getCatalogsByUserId(UUID userId) {
    return catalogRepository.findByJastiperId(userId);
  }

  public Catalog getCatalogById(UUID catalogId, User currentUser) {
    Catalog catalog =
        catalogRepository
            .findById(catalogId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));

    if (!catalog.getJastiper().getId().equals(currentUser.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, AUTH_FAILED_MESSAGE);
    }

    return catalog;
  }

  public Catalog updateCatalog(UUID catalogId, UpdateCatalogCommand command, User currentUser) {
    Catalog catalog =
        catalogRepository
            .findById(catalogId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));

    if (!catalog.getJastiper().getId().equals(currentUser.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, AUTH_FAILED_MESSAGE);
    }

    catalogFactory.applyUpdate(catalog, command);
    return catalogRepository.save(catalog);
  }

  public void deleteCatalog(UUID catalogId, User currentUser) {
    Catalog catalog =
        catalogRepository
            .findById(catalogId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));

    if (!catalog.getJastiper().getId().equals(currentUser.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, AUTH_FAILED_MESSAGE);
    }

    catalogRepository.deleteById(catalogId);
  }

  public Catalog decreaseStock(UUID catalogId, int quantity) {
    if (quantity <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
    }

    Catalog catalog =
        catalogRepository
            .findById(catalogId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));

    if (catalog.getStock() < quantity) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
    }

    catalog.setStock(catalog.getStock() - quantity);
    return catalogRepository.save(catalog);
  }

  private String normalizeSearchTerm(String value) {
    if (value == null) {
      return null;
    }

    String trimmedValue = value.trim();
    return trimmedValue.isEmpty() ? null : trimmedValue;
  }
}
