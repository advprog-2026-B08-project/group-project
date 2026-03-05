package id.ac.ui.cs.advprog.groupproject.catalog.service;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import id.ac.ui.cs.advprog.groupproject.catalog.repository.CatalogRepository;
import java.util.UUID;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.model.User;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class CatalogService {
    private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found";
    private static final String AUTH_FAILED_MESSAGE = "Auth Failed!";

    private final CatalogRepository catalogRepository;

    public CatalogService(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public Catalog createCatalog(Catalog catalog, User currentUser) {
        catalog.setJastiper(currentUser);
        return catalogRepository.save(catalog);
    }

    public List<Catalog> findAllCatalogs(User currentUser) {
        return catalogRepository.findByJastiperId(currentUser.getId());
    }

    public List<Catalog> getAllCatalogs() {
        return catalogRepository.findAll();
    }

    public List<Catalog> getCatalogsByUserId(UUID userId) {
        return catalogRepository.findByJastiperId(userId);
    }

    public Catalog getCatalogById(UUID catalogId, User currentUser) {
        Catalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));
        
        if (!catalog.getJastiper().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, AUTH_FAILED_MESSAGE);
        }
        
        return catalog;
    }

    public Catalog updateCatalog(UUID catalogId, Catalog newData, User currentUser) {
        Catalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));
        
        if (!catalog.getJastiper().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, AUTH_FAILED_MESSAGE);
        }
        
        catalog.setName(newData.getName());
        catalog.setDescription(newData.getDescription());
        catalog.setImageUrl(newData.getImageUrl());
        catalog.setPrice(newData.getPrice());
        catalog.setStock(newData.getStock());
        catalog.setOriginLocation(newData.getOriginLocation());
        catalog.setTravelDate(newData.getTravelDate());
        return catalogRepository.save(catalog);
    }

    public void deleteCatalog(UUID catalogId, User currentUser) {
        Catalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));

        if (!catalog.getJastiper().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, AUTH_FAILED_MESSAGE);
        }

        catalogRepository.deleteById(catalogId);
    }
}