package id.ac.ui.cs.advprog.groupproject.catalog.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class DefaultCatalogStrategyTest {

    private DefaultCatalogStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DefaultCatalogStrategy();
    }

    private User userWithRole(String role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(role);
        return user;
    }

    // ── canCreateCatalog ─────────────────────────

    @Test
    void canCreateCatalog_jastiper_returnsTrue() {
        assertTrue(strategy.canCreateCatalog(userWithRole("ROLE_JASTIPER")));
        assertTrue(strategy.canCreateCatalog(userWithRole("JASTIPER")));
    }

    @Test
    void canCreateCatalog_titiper_returnsFalse() {
        assertFalse(strategy.canCreateCatalog(userWithRole("ROLE_TITIPER")));
    }

    @Test
    void canCreateCatalog_nullUser_returnsFalse() {
        assertFalse(strategy.canCreateCatalog(null));
    }

    @Test
    void requireCanCreateCatalog_titiper_throws403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> strategy.requireCanCreateCatalog(userWithRole("ROLE_TITIPER")));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ── canSubmitRating ──────────────────────────

    @Test
    void canSubmitRating_titiperWithMatchingBuyerId_returnsTrue() {
        User user = userWithRole("ROLE_TITIPER");
        assertTrue(strategy.canSubmitRating(user, user.getId()));
    }

    @Test
    void canSubmitRating_titiperWithMismatchedBuyerId_returnsFalse() {
        User user = userWithRole("ROLE_TITIPER");
        assertFalse(strategy.canSubmitRating(user, UUID.randomUUID()));
    }

    @Test
    void canSubmitRating_jastiper_returnsFalse() {
        User user = userWithRole("ROLE_JASTIPER");
        assertFalse(strategy.canSubmitRating(user, user.getId()));
    }

    @Test
    void requireCanSubmitRating_buyerMismatch_throws403() {
        User user = userWithRole("ROLE_TITIPER");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> strategy.requireCanSubmitRating(user, UUID.randomUUID()));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ── canManageCatalog ─────────────────────────

    @Test
    void canManageCatalog_owner_returnsTrue() {
        User owner = userWithRole("ROLE_JASTIPER");
        Catalog catalog = new Catalog();
        catalog.setJastiper(owner);
        assertTrue(strategy.canManageCatalog(owner, catalog));
    }

    @Test
    void canManageCatalog_nonOwner_returnsFalse() {
        User owner = userWithRole("ROLE_JASTIPER");
        User other = userWithRole("ROLE_JASTIPER");
        Catalog catalog = new Catalog();
        catalog.setJastiper(owner);
        assertFalse(strategy.canManageCatalog(other, catalog));
    }

    @Test
    void canManageCatalog_nullCatalog_returnsFalse() {
        User user = userWithRole("ROLE_JASTIPER");
        assertFalse(strategy.canManageCatalog(user, null));
    }

    @Test
    void requireCanManageCatalog_nonOwner_throws403() {
        User owner = userWithRole("ROLE_JASTIPER");
        User other = userWithRole("ROLE_JASTIPER");
        Catalog catalog = new Catalog();
        catalog.setJastiper(owner);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> strategy.requireCanManageCatalog(other, catalog));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}
