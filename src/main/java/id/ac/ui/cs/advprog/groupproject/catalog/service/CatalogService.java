package id.ac.ui.cs.advprog.groupproject.catalog.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.LogType;
import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.ActionLogService;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.CreateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateResponse;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.UpdateCatalogRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.factory.CatalogFactory;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.catalog.model.CatalogRatingEvent;
import id.ac.ui.cs.advprog.groupproject.catalog.model.StockDecreaseEvent;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRatingEventRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.repository.StockDecreaseEventRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.strategy.CatalogActionStrategy;
import id.ac.ui.cs.advprog.groupproject.catalog.template.IdempotentTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
  private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found";
  private static final String CREATE_CATALOG_ACTION = "CREATE_CATALOG";
  private static final String UPDATE_CATALOG_ACTION = "UPDATE_CATALOG";
  private static final String DELETE_CATALOG_ACTION = "DELETE_CATALOG";
  private static final String METRIC_TAG_REASON = "reason";
  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogService.class);

  private final CatalogRepository catalogRepository;
  private final CatalogRatingEventRepository catalogRatingEventRepository;
  private final StockDecreaseEventRepository stockDecreaseEventRepository;
  private final CatalogFactory catalogFactory;
  private final CatalogActionStrategy catalogStrategy;
  private final MeterRegistry meterRegistry;
  private final ActionLogService actionLogService;

  public CatalogService(
      CatalogRepository catalogRepository,
      CatalogRatingEventRepository catalogRatingEventRepository,
      StockDecreaseEventRepository stockDecreaseEventRepository,
      CatalogFactory catalogFactory,
      CatalogActionStrategy catalogStrategy,
      MeterRegistry meterRegistry,
      ActionLogService actionLogService) {
    this.catalogRepository = catalogRepository;
    this.catalogRatingEventRepository = catalogRatingEventRepository;
    this.stockDecreaseEventRepository = stockDecreaseEventRepository;
    this.catalogFactory = catalogFactory;
    this.catalogStrategy = catalogStrategy;
    this.meterRegistry = meterRegistry;
    this.actionLogService = actionLogService;
  }

  public Catalog createCatalog(CreateCatalogRequest request, User currentUser) {
    catalogStrategy.requireCanCreateCatalog(currentUser);

    Catalog catalog = catalogFactory.create(request, currentUser);
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
    Catalog catalog = requireCatalog(catalogId);
    catalogStrategy.requireCanManageCatalog(currentUser, catalog);
    return catalog;
  }

  public Catalog getCatalogByIdForAdmin(UUID catalogId) {
    return requireCatalog(catalogId);
  }

  public Catalog updateCatalog(UUID catalogId, UpdateCatalogRequest request, User currentUser) {
    Catalog catalog = requireCatalog(catalogId);
    catalogStrategy.requireCanManageCatalog(currentUser, catalog);

    catalogFactory.applyUpdate(catalog, request);
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

  public Catalog updateCatalogByAdmin(UUID catalogId, UpdateCatalogRequest request) {
    Catalog catalog = requireCatalog(catalogId);

    catalogFactory.applyUpdate(catalog, request);
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
    Catalog catalog = requireCatalog(catalogId);
    catalogStrategy.requireCanManageCatalog(currentUser, catalog);

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
    Catalog catalog = requireCatalog(catalogId);
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

    return new IdempotentTemplate<UUID, Catalog>() {
      @Override
      protected String operationName() {
        return "stock_decrease";
      }

      @Override
      protected boolean isAlreadyProcessed(UUID key) {
        return stockDecreaseEventRepository.existsByRequestId(key);
      }

      @Override
      protected void recordEvent(UUID key) {
        StockDecreaseEvent event = new StockDecreaseEvent();
        event.setRequestId(key);
        event.setCatalogId(catalogId);
        event.setQuantity(quantity);
        event.setCreatedAt(Instant.now());
        stockDecreaseEventRepository.saveAndFlush(event);
      }

      @Override
      protected Catalog performAction(UUID key) {
        int updatedRows = catalogRepository.decreaseStockIfAvailable(catalogId, quantity);
        if (updatedRows == 0) {
          if (!catalogRepository.existsById(catalogId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE);
          }
          throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
        }
        return requireCatalog(catalogId);
      }

      @Override
      protected Catalog buildDuplicateResponse(UUID key) {
        return requireCatalog(catalogId);
      }
    }.execute(requestId);
  }

  @Transactional
  public ProductRatingUpdateResponse applyProductRating(
      UUID catalogId, ProductRatingUpdateRequest request, User currentUser) {
    if (!catalogStrategy.canSubmitRating(currentUser, request.getBuyerId())) {
      String reason =
          (currentUser == null || !Role.ROLE_TITIPER.matches(currentUser.getRole()))
              ? "role_forbidden"
              : "buyer_mismatch";
      meterRegistry.counter("catalog.rating.rejected", METRIC_TAG_REASON, reason).increment();
      catalogStrategy.requireCanSubmitRating(currentUser, request.getBuyerId());
    }

    return new IdempotentTemplate<UUID, ProductRatingUpdateResponse>() {
      @Override
      protected String operationName() {
        return "catalog_rating";
      }

      @Override
      protected boolean isAlreadyProcessed(UUID orderId) {
        return catalogRatingEventRepository.existsByOrderId(orderId);
      }

      @Override
      protected void recordEvent(UUID orderId) {
        if (!catalogRepository.existsById(catalogId)) {
          meterRegistry.counter("catalog.rating.rejected", METRIC_TAG_REASON, "catalog_not_found").increment();
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE);
        }
        CatalogRatingEvent event = new CatalogRatingEvent();
        event.setOrderId(orderId);
        event.setCatalogId(catalogId);
        event.setBuyerId(request.getBuyerId());
        event.setProductRating(request.getProductRating());
        event.setCreatedAt(Instant.now());
        catalogRatingEventRepository.save(event);
      }

      @Override
      protected ProductRatingUpdateResponse performAction(UUID orderId) {
        int updatedRows =
            catalogRepository.applyProductRating(catalogId, request.getProductRating());
        if (updatedRows == 0) {
          meterRegistry.counter("catalog.rating.failed", METRIC_TAG_REASON, "aggregate_update").increment();
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE);
        }
        Catalog catalog = requireCatalog(catalogId);
        meterRegistry.counter("catalog.rating.applied").increment();
        LOGGER.info(
            "catalog_rating_applied orderId={} catalogId={} rating={} ratingAverage={} ratingCount={}",
            orderId, catalogId, request.getProductRating(),
            catalog.getRatingAverage(), catalog.getRatingCount());
        return new ProductRatingUpdateResponse(
            true, catalog.getRatingAverage(), catalog.getRatingCount());
      }

      @Override
      protected ProductRatingUpdateResponse buildDuplicateResponse(UUID orderId) {
        Catalog catalog = requireCatalog(catalogId);
        return new ProductRatingUpdateResponse(
            false, catalog.getRatingAverage(), catalog.getRatingCount());
      }

      @Override
      protected void onDuplicate(UUID orderId) {
        meterRegistry.counter("catalog.rating.duplicate").increment();
      }
    }.execute(request.getOrderId());
  }

  private String normalizeSearchTerm(String value) {
    if (value == null) {
      return null;
    }

    String trimmedValue = value.trim();
    return trimmedValue.isEmpty() ? null : trimmedValue;
  }

  private Catalog requireCatalog(UUID catalogId) {
    return catalogRepository
        .findById(catalogId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));
  }

  private String getCatalogTarget(Catalog catalog) {
    UUID catalogId = catalog.getId();
    return catalogId == null ? "UNKNOWN_CATALOG_ID" : catalogId.toString();
  }
}
