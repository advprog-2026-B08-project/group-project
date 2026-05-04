package id.ac.ui.cs.advprog.groupproject.catalog.service;

import id.ac.ui.cs.advprog.groupproject.catalog.command.CreateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.command.UpdateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateResponse;
import id.ac.ui.cs.advprog.groupproject.catalog.factory.CatalogFactory;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.catalog.model.CatalogRatingEvent;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRatingEventRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.UUID;
import java.util.List;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
  private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found";
  private static final String AUTH_FAILED_MESSAGE = "Auth Failed!";
  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogService.class);

  private final CatalogRepository catalogRepository;
  private final CatalogRatingEventRepository catalogRatingEventRepository;
  private final CatalogFactory catalogFactory;
  private final MeterRegistry meterRegistry;

  public CatalogService(
      CatalogRepository catalogRepository,
      CatalogRatingEventRepository catalogRatingEventRepository,
      CatalogFactory catalogFactory,
      MeterRegistry meterRegistry) {
    this.catalogRepository = catalogRepository;
    this.catalogRatingEventRepository = catalogRatingEventRepository;
    this.catalogFactory = catalogFactory;
    this.meterRegistry = meterRegistry;
  }

  public Catalog createCatalog(CreateCatalogCommand command, User currentUser) {
    if (!isJastiper(currentUser)) {
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

  public Catalog getCatalogByIdForAdmin(UUID catalogId) {
    return catalogRepository
        .findById(catalogId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));
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

  public Catalog updateCatalogByAdmin(UUID catalogId, UpdateCatalogCommand command) {
    Catalog catalog =
        catalogRepository
            .findById(catalogId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));

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

  public void deleteCatalogByAdmin(UUID catalogId) {
    if (!catalogRepository.existsById(catalogId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE);
    }
    catalogRepository.deleteById(catalogId);
  }

  @Transactional
  public Catalog decreaseStock(UUID catalogId, int quantity) {
    if (quantity <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
    }

    int updatedRows = catalogRepository.decreaseStockIfAvailable(catalogId, quantity);
    if (updatedRows == 0) {
      if (!catalogRepository.existsById(catalogId)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE);
      }
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
    }

    return catalogRepository
        .findById(catalogId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));
  }

  @Transactional
  public ProductRatingUpdateResponse applyProductRating(
      UUID catalogId, ProductRatingUpdateRequest request, User currentUser) {
    if (!isTitiper(currentUser)) {
      meterRegistry.counter("catalog.rating.rejected", "reason", "role_forbidden").increment();
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only Titiper can submit product rating");
    }

    if (!currentUser.getId().equals(request.getBuyerId())) {
      meterRegistry.counter("catalog.rating.rejected", "reason", "buyer_mismatch").increment();
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Buyer mismatch");
    }

    if (catalogRatingEventRepository.existsByOrderId(request.getOrderId())) {
      meterRegistry.counter("catalog.rating.duplicate").increment();
      LOGGER.info("catalog_rating_duplicate orderId={} catalogId={}", request.getOrderId(), catalogId);
      Catalog catalog = getCatalogByIdForAdmin(catalogId);
      return new ProductRatingUpdateResponse(false, catalog.getRatingAverage(), catalog.getRatingCount());
    }

    if (!catalogRepository.existsById(catalogId)) {
      meterRegistry.counter("catalog.rating.rejected", "reason", "catalog_not_found").increment();
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE);
    }

    CatalogRatingEvent event = new CatalogRatingEvent();
    event.setOrderId(request.getOrderId());
    event.setCatalogId(catalogId);
    event.setBuyerId(request.getBuyerId());
    event.setProductRating(request.getProductRating());
    event.setCreatedAt(Instant.now());

    try {
      catalogRatingEventRepository.save(event);
    } catch (DataIntegrityViolationException ex) {
      meterRegistry.counter("catalog.rating.duplicate").increment();
      LOGGER.info(
          "catalog_rating_duplicate_race orderId={} catalogId={}", request.getOrderId(), catalogId);
      Catalog catalog = getCatalogByIdForAdmin(catalogId);
      return new ProductRatingUpdateResponse(false, catalog.getRatingAverage(), catalog.getRatingCount());
    }

    int updatedRows = catalogRepository.applyProductRating(catalogId, request.getProductRating());
    if (updatedRows == 0) {
      meterRegistry.counter("catalog.rating.failed", "reason", "aggregate_update").increment();
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE);
    }

    Catalog catalog = getCatalogByIdForAdmin(catalogId);
    meterRegistry.counter("catalog.rating.applied").increment();
    LOGGER.info(
        "catalog_rating_applied orderId={} catalogId={} rating={} ratingAverage={} ratingCount={}",
        request.getOrderId(),
        catalogId,
        request.getProductRating(),
        catalog.getRatingAverage(),
        catalog.getRatingCount());

    return new ProductRatingUpdateResponse(true, catalog.getRatingAverage(), catalog.getRatingCount());
  }

  private String normalizeSearchTerm(String value) {
    if (value == null) {
      return null;
    }

    String trimmedValue = value.trim();
    return trimmedValue.isEmpty() ? null : trimmedValue;
  }

  private boolean isJastiper(User user) {
    String role = user.getRole();
    return "JASTIPER".equalsIgnoreCase(role) || "ROLE_JASTIPER".equalsIgnoreCase(role);
  }

  private boolean isTitiper(User user) {
    String role = user.getRole();
    return "TITIPER".equalsIgnoreCase(role) || "ROLE_TITIPER".equalsIgnoreCase(role);
  }
}
