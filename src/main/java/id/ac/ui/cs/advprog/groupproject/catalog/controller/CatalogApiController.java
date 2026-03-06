package id.ac.ui.cs.advprog.groupproject.controller;

import id.ac.ui.cs.advprog.groupproject.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.model.User;
//import id.ac.ui.cs.advprog.groupproject.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private CatalogService catalogService;

//    @Autowired
//    private UserRepository userRepository;

    private User getCurrentUser(Principal principal) {
//        return userRepository.findByUsername(principal.getName())
//            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));

        return new User(); // Trik sementara biar project bisa di-build
    }
    
    @GetMapping
    public ResponseEntity<List<Catalog>> getAllMyCatalogs(Principal principal) {
        User currentUser = getCurrentUser(principal);
        List<Catalog> catalogs = catalogService.findAllCatalogs(currentUser);
        return ResponseEntity.ok(catalogs);
    }
    
    @PostMapping
    public ResponseEntity<Catalog> createCatalog(@Valid @RequestBody Catalog catalog, Principal principal) {
        User currentUser = getCurrentUser(principal);
        
        if (!currentUser.isJastiper()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Jastiper can create catalog");
        }
        
        Catalog createdCatalog = catalogService.createCatalog(catalog, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCatalog);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Catalog> updateCatalog(
            @PathVariable UUID id,
            @Valid @RequestBody Catalog catalog,
            Principal principal) {
        User currentUser = getCurrentUser(principal);
        Catalog updatedCatalog = catalogService.updateCatalog(id, catalog, currentUser);
        return ResponseEntity.ok(updatedCatalog);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCatalog(@PathVariable UUID id, Principal principal) {
        User currentUser = getCurrentUser(principal);
        catalogService.deleteCatalog(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
