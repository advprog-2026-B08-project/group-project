package id.ac.ui.cs.advprog.groupproject.catalog.policy;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import java.util.UUID;

/**
 * Strategy that encapsulates authorization rules for catalog operations.
 * Implementations decide which roles or relationships are allowed to perform each action.
 *
 * <p>The {@code requireXxx} methods throw a {@code ResponseStatusException} (typically 403)
 * when the policy denies the action; the corresponding {@code canXxx} predicates simply
 * return a boolean and never throw.</p>
 */
public interface CatalogActionPolicy {

    boolean canCreateCatalog(User user);

    boolean canSubmitRating(User user, UUID buyerId);

    boolean canManageCatalog(User user, Catalog catalog);

    void requireCanCreateCatalog(User user);

    void requireCanSubmitRating(User user, UUID buyerId);

    void requireCanManageCatalog(User user, Catalog catalog);
}
