package id.ac.ui.cs.advprog.groupproject.catalog.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.LogType;
import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.ActionLogService;
import id.ac.ui.cs.advprog.groupproject.catalog.command.CreateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.command.UpdateCatalogCommand;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateResponse;
import id.ac.ui.cs.advprog.groupproject.catalog.factory.CatalogFactory;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.catalog.model.CatalogRatingEvent;
import id.ac.ui.cs.advprog.groupproject.catalog.model.StockDecreaseEvent;
import id.ac.ui.cs.advprog.groupproject.catalog.policy.CatalogActionPolicy;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRatingEventRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.StockDecreaseEventRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
  private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found";
  private static final String CREATE_CATALOG_ACTION = "CREATE_CATALOG";
  private static final String UPDATE_CATALOG_ACTION = "UPDATE_CATALOG";
  private static final String DELETE_CATALOG_ACTION = "DELETE_CATALOG";
  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogService.class);

  private final CatalogRepository catalogRepository;
  private final CatalogRatingEventRepository catalogRatingEventRepository;
  private final StockDecreaseEventRepository stockDecreaseEventRepository;
  private final CatalogFactory catalogFactory;
  private final CatalogActionPolicy catalogPolicy;
  private final MeterRegistry meterRegistry;
  private final ActionLogService actionLogService;

  public CatalogService(
      CatalogRepository catalogRepository,
      CatalogRatingEventRepository catalogRatingEventRepository,
      StockDecreaseEventRepository stockDecreaseEventRepository,
      CatalogFactory catalogFactory,
      CatalogActionPolicy catalogPolicy,
      MeterRegistry meterRegistry,
      ActionLogService actionLogService) {
    this.catalogRepository = catalogRepository;
    this.catalogRatingEventRepository = catalogRatingEventRepository;
    this.stockDecreaseEventRepository = stockDecreaseEventRepository;
    this.catalogFactory = catalogFactory;
    this.catalogPolicy = catalogPolicy;
    this.meterRegistry = meterRegistry;
    this.actionLogService = actionLogService;
  }

  public Catalog createCatalog(CreateCatalogCommand command, User currentUser) {
    catalogPolicy.requireCanCreateCatalog(currentUser);

    Catalog catalog = catalogFactory.create(command, currentUser);
    Catalog savedCatalog = catalogRepository.save(catalog);
    actionLogService.log(
        CREATE_CATALOG_ACTION,
        currentUser.getUsername(),
        currentUser.getRole(),
        getCatalogTarget(savedCatalog),
        "Created catalog '" + savedCatalog.getName() + "'",
            LogType.INFO);
    return savedCatalog;
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

    catalogPolicy.requireCanManageCatalog(currentUser, catalog);
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

    catalogPolicy.requireCanManageCatalog(currentUser, catalog);

    catalogFactory.applyUpdate(catalog, command);
    Catalog updatedCatalog = catalogRepository.save(catalog);
    actionLogService.log(
        UPDATE_CATALOG_ACTION,
        currentUser.getUsername(),
        currentUser.getRole(),
        getCatalogTarget(updatedCatalog),
        "Updated catalog '" + updatedCatalog.getName() + "'",
            LogType.INFO);
    return updatedCatalog;
  }

  public Catalog updateCatalogByAdmin(UUID catalogId, UpdateCatalogCommand command) {
    Catalog catalog =
        catalogRepository
            .findById(catalogId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));

    catalogFactory.applyUpdate(catalog, command);
    Catalog updatedCatalog = catalogRepository.save(catalog);
    actionLogService.log(
        UPDATE_CATALOG_ACTION,
        "SYSTEM_ADMIN",
        "ADMIN",
        getCatalogTarget(updatedCatalog),
        "Admin updated catalog '" + updatedCatalog.getName() + "'",
            LogType.INFO);
    return updatedCatalog;
  }

  public void deleteCatalog(UUID catalogId, User currentUser) {
    Catalog catalog =
        catalogRepository
            .findById(catalogId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));

    catalogPolicy.requireCanManageCatalog(currentUser, catalog);

    catalogRepository.deleteById(catalogId);
    actionLogService.log(
        DELETE_CATALOG_ACTION,
        currentUser.getUsername(),
        currentUser.getRole(),
        catalogId.toString(),
        "Deleted catalog '" + catalog.getName() + "'",
            LogType.WARN);
  }

  public void deleteCatalogByAdmin(UUID catalogId) {
    Catalog catalog =
        catalogRepository
            .findById(catalogId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));
    catalogRepository.deleteById(catalogId);
    actionLogService.log(
        DELETE_CATALOG_ACTION,
        "SYSTEM_ADMIN",
        "ADMIN",
        catalogId.toString(),
        "Admin deleted catalog '" + catalog.getName() + "'",
            LogType.WARN);
  }

  @Transactional
  public Catalog decreaseStock(UUID catalogId, UUID requestId, int quantity) {
    if (quantity <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
    }

    if (stockDecreaseEventRepository.existsByRequestId(requestId)) {
      LOGGER.info("stock_decrease_duplicate requestId={} catalogId={}", requestId, catalogId);
      return catalogRepository
          .findById(catalogId)
          .orElseThrow(
              () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));
    }

    StockDecreaseEvent event = new StockDecreaseEvent();
    event.setRequestId(requestId);
    event.setCatalogId(catalogId);
    event.setQuantity(quantity);
    event.setCreatedAt(Instant.now());

    try {
      stockDecreaseEventRepository.saveAndFlush(event);
    } catch (DataIntegrityViolationException ex) {
      LOGGER.info("stock_decrease_duplicate_race requestId={} catalogId={}", requestId, catalogId);
      return catalogRepository
          .findById(catalogId)
          .orElseThrow(
              () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));
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
    if (!catalogPolicy.canSubmitRating(currentUser, request.getBuyerId())) {
      String reason =
          (currentUser == null || !Role.ROLE_TITIPER.matches(currentUser.getRole()))
              ? "role_forbidden"
              : "buyer_mismatch";
      meterRegistry.counter("catalog.rating.rejected", "reason", reason).increment();
      // Delegate to policy for the canonical exception/message
      catalogPolicy.requireCanSubmitRating(currentUser, request.getBuyerId());
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

  private String getCatalogTarget(Catalog catalog) {
    UUID catalogId = catalog.getId();
    return catalogId == null ? "UNKNOWN_CATALOG_ID" : catalogId.toString();
  }
}
