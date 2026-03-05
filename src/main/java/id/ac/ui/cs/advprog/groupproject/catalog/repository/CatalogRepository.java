package id.ac.ui.cs.advprog.groupproject.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;

public interface CatalogRepository extends JpaRepository<Catalog, UUID> {
    List<Catalog> findByJastiperId(UUID jastiperId);
}