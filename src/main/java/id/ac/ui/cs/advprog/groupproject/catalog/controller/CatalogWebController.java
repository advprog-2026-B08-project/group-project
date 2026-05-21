package id.ac.ui.cs.advprog.groupproject.catalog.controller;

import id.ac.ui.cs.advprog.groupproject.catalog.dto.CatalogDto;
import id.ac.ui.cs.advprog.groupproject.catalog.mapper.CatalogMapper;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.catalog.service.CatalogService;
import id.ac.ui.cs.advprog.groupproject.catalog.service.CatalogImageService;
import id.ac.ui.cs.advprog.groupproject.order.repository.OrderRepository;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/catalog")
public class CatalogWebController {
  private static final String CATALOGS_ATTRIBUTE = "catalogs";

  private final CatalogService catalogService;
  private final CatalogImageService catalogImageService;
  private final CatalogMapper catalogMapper;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;

  public CatalogWebController(
      CatalogService catalogService,
      CatalogImageService catalogImageService,
      CatalogMapper catalogMapper,
      UserRepository userRepository,
      OrderRepository orderRepository) {
    this.catalogService = catalogService;
    this.catalogImageService = catalogImageService;
    this.catalogMapper = catalogMapper;
    this.userRepository = userRepository;
    this.orderRepository = orderRepository;
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

  private boolean isJastiper(User user) {
    String role = user.getRole();
    return "JASTIPER".equalsIgnoreCase(role) || "ROLE_JASTIPER".equalsIgnoreCase(role);
  }

  private boolean isAdmin(User user) {
    String role = user.getRole();
    return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
  }

  @GetMapping
  public String catalog(Model model, Principal principal) {
    List<CatalogDto> catalogs = catalogMapper.toDtoList(catalogService.getAllCatalogs());
    enrichWithJastiperRating(catalogs);
    model.addAttribute(CATALOGS_ATTRIBUTE, catalogs);
    if (principal != null) {
      userRepository
          .findByUsername(principal.getName())
          .ifPresent(user -> {
            model.addAttribute("currentUserId", user.getId().toString());
            model.addAttribute("userRole", user.getRole());
          });
    }
    return "catalog/html/catalog";
  }

  @GetMapping("/detail/{id}")
  public String catalogDetail(@PathVariable UUID id, Model model, Principal principal) {
    Catalog catalog = catalogService.getCatalogByIdForAdmin(id);
    model.addAttribute("catalog", catalogMapper.toDto(catalog));
    if (principal != null) {
      userRepository
          .findByUsername(principal.getName())
          .ifPresent(user -> model.addAttribute("currentUserId", user.getId().toString()));
    }
    return "catalog/html/detailCatalog";
  }

  @GetMapping("/{userId}")
  public String userCatalog(@PathVariable UUID userId, Model model) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    List<CatalogDto> catalogs = catalogMapper.toDtoList(catalogService.getCatalogsByUserId(userId));
    enrichWithJastiperRating(catalogs);
    model.addAttribute(CATALOGS_ATTRIBUTE, catalogs);
    model.addAttribute("username", user.getUsername());
    model.addAttribute("jastiperUser", user);
    return "catalog/html/userCatalog";
  }

  @GetMapping("/my")
  public String myCatalog(Model model, Principal principal) {
    User currentUser = getCurrentUser(principal);

    model.addAttribute(
        CATALOGS_ATTRIBUTE, catalogMapper.toDtoList(catalogService.findAllCatalogs(currentUser)));
    model.addAttribute("username", currentUser.getUsername());
    return "catalog/html/myCatalog";
  }

  @GetMapping("/edit/{id}")
  public String editCatalog(@PathVariable UUID id, Model model, Principal principal) {
    User currentUser = getCurrentUser(principal);

    Catalog catalog = catalogService.getCatalogById(id, currentUser);
    model.addAttribute("catalog", catalogMapper.toDto(catalog));
    return "catalog/html/editCatalog";
  }

  @PostMapping("/edit")
  public String updateCatalog(
      @Valid @ModelAttribute("catalog") CatalogDto catalogDto,
      org.springframework.validation.BindingResult result,
      @RequestParam(name = "file", required = false) MultipartFile file,
      Principal principal) {
    if (result.hasErrors()) {
      return "catalog/html/editCatalog";
    }
    User currentUser = getCurrentUser(principal);

    if (file != null && !file.isEmpty()) {
      catalogDto.setImageUrl(catalogImageService.uploadCatalogImage(file));
    }

    catalogService.updateCatalog(
        catalogDto.getId(), catalogMapper.toUpdateCommand(catalogDto), currentUser);
    return "redirect:/catalog/my";
  }

  @GetMapping("/add")
  public String addCatalogPage(Model model, Principal principal) {
    User currentUser = getCurrentUser(principal);

    if (!isJastiper(currentUser)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Jastiper can create catalog");
    }

    model.addAttribute("catalog", new CatalogDto());
    return "catalog/html/addCatalog";
  }

  @PostMapping("/add")
  public String createCatalog(
      @Valid @ModelAttribute("catalog") CatalogDto catalogDto,
      org.springframework.validation.BindingResult result,
      @RequestParam(name = "file", required = false) MultipartFile file,
      Principal principal) {
    if (result.hasErrors()) {
      return "catalog/html/addCatalog";
    }
    User currentUser = getCurrentUser(principal);

    if (!isJastiper(currentUser)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Jastiper can create catalog");
    }

    if (file != null && !file.isEmpty()) {
      catalogDto.setImageUrl(catalogImageService.uploadCatalogImage(file));
    }

    catalogService.createCatalog(catalogMapper.toCreateCommand(catalogDto), currentUser);
    return "redirect:/catalog/my";
  }

  @GetMapping("/admin/monitoring")
  public String adminCatalogMonitoring(Model model, Principal principal) {
    User currentUser = getCurrentUser(principal);
    if (!isAdmin(currentUser)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can access monitoring");
    }

    model.addAttribute(
        CATALOGS_ATTRIBUTE, catalogMapper.toDtoList(catalogService.getAllCatalogs()));
    return "catalog/html/adminCatalogMonitoring";
  }

  @GetMapping("/admin/edit/{id}")
  public String adminEditCatalog(@PathVariable UUID id, Model model, Principal principal) {
    User currentUser = getCurrentUser(principal);
    if (!isAdmin(currentUser)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can edit catalog");
    }

    Catalog catalog = catalogService.getCatalogByIdForAdmin(id);
    model.addAttribute("catalog", catalogMapper.toDto(catalog));
    return "catalog/html/editCatalog";
  }

  @PostMapping("/admin/edit")
  public String adminUpdateCatalog(
      @Valid @ModelAttribute("catalog") CatalogDto catalogDto,
      org.springframework.validation.BindingResult result,
      @RequestParam(name = "file", required = false) MultipartFile file,
      Principal principal) {
    if (result.hasErrors()) {
      return "catalog/html/editCatalog";
    }

    User currentUser = getCurrentUser(principal);
    if (!isAdmin(currentUser)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can edit catalog");
    }

    if (file != null && !file.isEmpty()) {
      catalogDto.setImageUrl(catalogImageService.uploadCatalogImage(file));
    }

    catalogService.updateCatalogByAdmin(catalogDto.getId(), catalogMapper.toUpdateCommand(catalogDto));
    return "redirect:/catalog/admin/monitoring";
  }

  @PostMapping("/admin/delete/{id}")
  public String adminDeleteCatalog(@PathVariable UUID id, Principal principal) {
    User currentUser = getCurrentUser(principal);
    if (!isAdmin(currentUser)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can delete catalog");
    }

    catalogService.deleteCatalogByAdmin(id);
    return "redirect:/catalog/admin/monitoring";
  }

  private void enrichWithJastiperRating(List<CatalogDto> catalogs) {
    for (CatalogDto dto : catalogs) {
      if (dto.getJastiperId() != null) {
        try {
          Double avgRating = orderRepository.findAverageJastiperRating(dto.getJastiperId());
          dto.setJastiperRatingAverage(avgRating);
        } catch (Exception e) {
          // Silently ignore if order data not available
        }
      }
    }
  }
}
