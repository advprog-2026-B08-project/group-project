package id.ac.ui.cs.advprog.groupproject.catalog.controller;

import id.ac.ui.cs.advprog.groupproject.catalog.dto.CatalogDto;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.DecreaseStockRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateRequest;
import id.ac.ui.cs.advprog.groupproject.catalog.dto.ProductRatingUpdateResponse;
import id.ac.ui.cs.advprog.groupproject.catalog.mapper.CatalogMapper;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalogs")
public class CatalogApiController {

  private final CatalogService catalogService;
  private final CatalogMapper catalogMapper;
  private final UserRepository userRepository;

  public CatalogApiController(
      CatalogService catalogService, CatalogMapper catalogMapper, UserRepository userRepository) {
    this.catalogService = catalogService;
    this.catalogMapper = catalogMapper;
    this.userRepository = userRepository;
  }

  private User getCurrentUser(Principal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    return userRepository
        .findByUsername(principal.getName())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
  }

  @GetMapping
  public ResponseEntity<List<CatalogDto>> getAllMyCatalogs(Principal principal) {
    User currentUser = getCurrentUser(principal);
    List<Catalog> catalogs = catalogService.findAllCatalogs(currentUser);
    return ResponseEntity.ok(catalogMapper.toDtoList(catalogs));
  }

  @GetMapping("/search")
  public ResponseEntity<List<CatalogDto>> searchCatalogs(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String jastiper) {
    List<Catalog> catalogs;
    if (keyword != null && !keyword.isBlank()) {
      catalogs = catalogService.searchCatalogs(keyword);
    } else {
      catalogs = catalogService.searchCatalogs(name, jastiper);
    }

    return ResponseEntity.ok(catalogMapper.toDtoList(catalogs));
  }

  @PostMapping
  public ResponseEntity<CatalogDto> createCatalog(
      @Valid @RequestBody CatalogDto catalogDto, Principal principal) {
    User currentUser = getCurrentUser(principal);

    if (!currentUser.isJastiper()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Jastiper can create catalog");
    }

    Catalog createdCatalog =
        catalogService.createCatalog(catalogMapper.toCreateCommand(catalogDto), currentUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogMapper.toDto(createdCatalog));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CatalogDto> updateCatalog(
      @PathVariable UUID id, @Valid @RequestBody CatalogDto catalogDto, Principal principal) {
    User currentUser = getCurrentUser(principal);
    Catalog updatedCatalog =
        catalogService.updateCatalog(id, catalogMapper.toUpdateCommand(catalogDto), currentUser);
    return ResponseEntity.ok(catalogMapper.toDto(updatedCatalog));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCatalog(@PathVariable UUID id, Principal principal) {
    User currentUser = getCurrentUser(principal);
    catalogService.deleteCatalog(id, currentUser);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/decrease-stock")
  public ResponseEntity<CatalogDto> decreaseStock(
      @PathVariable UUID id, @Valid @RequestBody DecreaseStockRequest request) {
    Catalog updatedCatalog = catalogService.decreaseStock(id, request.getQuantity());
    return ResponseEntity.ok(catalogMapper.toDto(updatedCatalog));
  }

  @PostMapping("/{id}/ratings")
  public ResponseEntity<ProductRatingUpdateResponse> applyProductRating(
      @PathVariable UUID id,
      @Valid @RequestBody ProductRatingUpdateRequest request,
      Principal principal) {
    User currentUser = getCurrentUser(principal);
    ProductRatingUpdateResponse response = catalogService.applyProductRating(id, request, currentUser);
    return ResponseEntity.ok(response);
  }
}
