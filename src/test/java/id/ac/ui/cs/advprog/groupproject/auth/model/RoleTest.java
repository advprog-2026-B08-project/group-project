package id.ac.ui.cs.advprog.groupproject.auth.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void matches_canonicalForm_returnsTrue() {
        assertTrue(Role.ROLE_JASTIPER.matches("ROLE_JASTIPER"));
        assertTrue(Role.ROLE_TITIPER.matches("ROLE_TITIPER"));
        assertTrue(Role.ROLE_ADMIN.matches("ROLE_ADMIN"));
    }

    @Test
    void matches_bareForm_returnsTrue() {
        assertTrue(Role.ROLE_JASTIPER.matches("JASTIPER"));
        assertTrue(Role.ROLE_TITIPER.matches("TITIPER"));
        assertTrue(Role.ROLE_ADMIN.matches("ADMIN"));
    }

    @Test
    void matches_isCaseInsensitive() {
        assertTrue(Role.ROLE_JASTIPER.matches("jastiper"));
        assertTrue(Role.ROLE_JASTIPER.matches("Role_Jastiper"));
        assertTrue(Role.ROLE_TITIPER.matches("titiper"));
    }

    @Test
    void matches_trimsWhitespace() {
        assertTrue(Role.ROLE_JASTIPER.matches("  JASTIPER  "));
    }

    @Test
    void matches_differentRole_returnsFalse() {
        assertFalse(Role.ROLE_JASTIPER.matches("TITIPER"));
        assertFalse(Role.ROLE_JASTIPER.matches("ROLE_ADMIN"));
        assertFalse(Role.ROLE_ADMIN.matches("ROLE_TITIPER"));
    }

    @Test
    void matches_nullOrBlank_returnsFalse() {
        assertFalse(Role.ROLE_JASTIPER.matches(null));
        assertFalse(Role.ROLE_JASTIPER.matches(""));
        assertFalse(Role.ROLE_JASTIPER.matches("   "));
    }

    @Test
    void matches_unknownRole_returnsFalse() {
        assertFalse(Role.ROLE_JASTIPER.matches("MODERATOR"));
        assertFalse(Role.ROLE_JASTIPER.matches("ROLE_UNKNOWN"));
    }
}
