package id.ac.ui.cs.advprog.groupproject.catalog.strategy;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import java.util.UUID;

public interface CatalogActionStrategy {

    boolean canCreateCatalog(User user);

    boolean canSubmitRating(User user, UUID buyerId);

    boolean canManageCatalog(User user, Catalog catalog);

    void requireCanCreateCatalog(User user);

    void requireCanSubmitRating(User user, UUID buyerId);

    void requireCanManageCatalog(User user, Catalog catalog);
}
