package id.ac.ui.cs.advprog.groupproject.controller;

import id.ac.ui.cs.advprog.groupproject.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.model.User;
import id.ac.ui.cs.advprog.groupproject.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/catalog")
public class CatalogWebController {
    private static final String CATALOGS_ATTRIBUTE = "catalogs";
    
    private final CatalogService catalogService;
    private final UserRepository userRepository;

    public CatalogWebController(CatalogService catalogService, UserRepository userRepository) {
        this.catalogService = catalogService;
        this.userRepository = userRepository;
    }
    
    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
    }
    
    @GetMapping
    public String catalog(Model model) {
        model.addAttribute(CATALOGS_ATTRIBUTE, catalogService.getAllCatalogs());
        return "catalog/catalog";
    }

    @GetMapping("/{userId}")
    public String userCatalog(@PathVariable UUID userId, Model model) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        model.addAttribute(CATALOGS_ATTRIBUTE, catalogService.getCatalogsByUserId(userId));
        model.addAttribute("username", user.getUsername());
        return "catalog/userCatalog";
    }

    @GetMapping("/my")
    public String myCatalog(Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        
        model.addAttribute(CATALOGS_ATTRIBUTE, catalogService.findAllCatalogs(currentUser));
        model.addAttribute("username", currentUser.getUsername());
        return "catalog/myCatalog";
    }
    
    @GetMapping("/edit/{id}")
    public String editCatalog(@PathVariable UUID id, Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        
        Catalog catalog = catalogService.getCatalogById(id, currentUser);
        model.addAttribute("catalog", catalog);
        return "catalog/editCatalog";
    }
    
    @PostMapping("/edit")
    public String updateCatalog(@Valid @ModelAttribute Catalog catalog, Principal principal) {
        User currentUser = getCurrentUser(principal);
        
        catalogService.updateCatalog(catalog.getId(), catalog, currentUser);
        return "redirect:/catalog/my";
    }
    
    @GetMapping("/add")
    public String addCatalogPage(Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        
        if (!currentUser.isJastiper()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Jastiper can create catalog");
        }
        
        model.addAttribute("catalog", new Catalog());
        return "catalog/addCatalog";
    }
    
    @PostMapping("/add")
    public String createCatalog(@Valid @ModelAttribute Catalog catalog, Principal principal) {
        User currentUser = getCurrentUser(principal);
        
        if (!currentUser.isJastiper()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Jastiper can create catalog");
        }
        
        catalogService.createCatalog(catalog, currentUser);
        return "redirect:/catalog/my";
    }
}
