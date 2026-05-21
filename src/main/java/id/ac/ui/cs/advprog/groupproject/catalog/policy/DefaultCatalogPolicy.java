package id.ac.ui.cs.advprog.groupproject.catalog.policy;

import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Default {@link CatalogActionPolicy} implementation backed by the {@link Role} enum.
 * Centralizes the authorization rules previously scattered across CatalogService.
 */
@Component
public class DefaultCatalogPolicy implements CatalogActionPolicy {

    private static final String ONLY_JASTIPER_CREATE = "Only Jastiper can create catalog";
    private static final String ONLY_TITIPER_RATE = "Only Titiper can submit product rating";
    private static final String BUYER_MISMATCH = "Buyer mismatch";
    private static final String NOT_OWNER = "Auth Failed!";

    @Override
    public boolean canCreateCatalog(User user) {
        return user != null && Role.ROLE_JASTIPER.matches(user.getRole());
    }

    @Override
    public boolean canSubmitRating(User user, UUID buyerId) {
        if (user == null || !Role.ROLE_TITIPER.matches(user.getRole())) {
            return false;
        }
        return user.getId() != null && user.getId().equals(buyerId);
    }

    @Override
    public boolean canManageCatalog(User user, Catalog catalog) {
        if (user == null || catalog == null || catalog.getJastiper() == null) {
            return false;
        }
        return catalog.getJastiper().getId().equals(user.getId());
    }

    @Override
    public void requireCanCreateCatalog(User user) {
        if (!canCreateCatalog(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ONLY_JASTIPER_CREATE);
        }
    }

    @Override
    public void requireCanSubmitRating(User user, UUID buyerId) {
        if (user == null || !Role.ROLE_TITIPER.matches(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ONLY_TITIPER_RATE);
        }
        if (user.getId() == null || !user.getId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, BUYER_MISMATCH);
        }
    }

    @Override
    public void requireCanManageCatalog(User user, Catalog catalog) {
        if (!canManageCatalog(user, catalog)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, NOT_OWNER);
        }
    }
}
