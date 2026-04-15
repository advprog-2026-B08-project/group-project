package id.ac.ui.cs.advprog.groupproject.catalog.repository;

import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.List;

public interface CatalogRepository extends JpaRepository<Catalog, UUID> {
    List<Catalog> findByJastiperId(UUID jastiperId);

    @Query("""
            SELECT c
            FROM Catalog c
            JOIN c.jastiper j
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(j.username) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<Catalog> searchCatalogsByKeyword(@Param("keyword") String keyword);

        @Query("""
                        SELECT c
                        FROM Catalog c
                        JOIN c.jastiper j
                        WHERE (:name IS NULL OR :name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
                            AND (:jastiper IS NULL OR :jastiper = '' OR LOWER(j.username) LIKE LOWER(CONCAT('%', :jastiper, '%')))
                        """)
        List<Catalog> searchCatalogs(@Param("name") String name, @Param("jastiper") String jastiper);
}