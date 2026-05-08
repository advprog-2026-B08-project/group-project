package id.ac.ui.cs.advprog.groupproject.catalog.repository;

import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalogRepository extends JpaRepository<Catalog, UUID> {
  List<Catalog> findByJastiperId(UUID jastiperId);

  @Query(
      """
      SELECT c
      FROM Catalog c
      JOIN c.jastiper j
      WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(j.username) LIKE LOWER(CONCAT('%', :keyword, '%')))
      """)
  List<Catalog> searchCatalogsByKeyword(@Param("keyword") String keyword);

  @Query(
      """
SELECT c
FROM Catalog c
JOIN c.jastiper j
WHERE (:name IS NULL OR :name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
  AND (:jastiper IS NULL OR :jastiper = '' OR LOWER(j.username) LIKE LOWER(CONCAT('%', :jastiper, '%')))
""")
  List<Catalog> searchCatalogs(@Param("name") String name, @Param("jastiper") String jastiper);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      UPDATE Catalog c
      SET c.stock = c.stock - :quantity
      WHERE c.id = :catalogId AND c.stock >= :quantity
      """)
  int decreaseStockIfAvailable(@Param("catalogId") UUID catalogId, @Param("quantity") int quantity);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      UPDATE Catalog c
      SET c.stock = c.stock + :quantity
      WHERE c.id = :catalogId
      """)
  int increaseStock(@Param("catalogId") UUID catalogId, @Param("quantity") int quantity);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      UPDATE Catalog c
      SET c.ratingSum = c.ratingSum + :productRating,
          c.ratingCount = c.ratingCount + 1,
          c.ratingAverage = (c.ratingSum + :productRating) * 1.0 / (c.ratingCount + 1)
      WHERE c.id = :catalogId
      """)
  int applyProductRating(@Param("catalogId") UUID catalogId, @Param("productRating") int productRating);
}

