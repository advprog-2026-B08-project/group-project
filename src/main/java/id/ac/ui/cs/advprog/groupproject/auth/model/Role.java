package id.ac.ui.cs.advprog.groupproject.auth.model;

public enum Role {
    ROLE_ADMIN,
    ROLE_JASTIPER,
    ROLE_TITIPER;

    /**
     * Returns true when the given role string matches this enum constant,
     * accepting both the canonical {@code ROLE_X} form and the bare {@code X} form
     * (case-insensitive). Returns false for {@code null} or empty input.
     */
    public boolean matches(String roleString) {
        if (roleString == null || roleString.isBlank()) {
            return false;
        }
        String normalized = roleString.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return name().equals(normalized);
    }
}
