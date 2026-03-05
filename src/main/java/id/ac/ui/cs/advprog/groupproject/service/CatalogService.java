package id.ac.ui.cs.advprog.groupproject.service;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import id.ac.ui.cs.advprog.groupproject.repository.CatalogRepository;
import java.util.UUID;
import id.ac.ui.cs.advprog.groupproject.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class CatalogService {
    @Autowired
    private CatalogRepository catalogRepository;

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
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        
        if (!catalog.getJastiper().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Auth Failed!");
        }
        
        return catalog;
    }

    public Catalog updateCatalog(UUID catalogId, Catalog newData, User currentUser) {
        Catalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        
        if (!catalog.getJastiper().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Auth Failed!");
        }
        
        catalog.setName(newData.getName());
        catalog.setDescription(newData.getDescription());
        catalog.setPrice(newData.getPrice());
        catalog.setStock(newData.getStock());
        catalog.setOriginLocation(newData.getOriginLocation());
        catalog.setTravelDate(newData.getTravelDate());
        return catalogRepository.save(catalog);
    }

    public void deleteCatalog(UUID catalogId, User currentUser) {
        Catalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        if (!catalog.getJastiper().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Auth Failed!");
        }

        catalogRepository.deleteById(catalogId);
    }
}