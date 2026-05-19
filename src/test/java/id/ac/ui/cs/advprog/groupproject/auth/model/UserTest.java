package id.ac.ui.cs.advprog.groupproject.auth.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    @Test
    public void testIsJastiperTrue() {
        User user = new User();
        user.setRole(Role.ROLE_JASTIPER.toString());

        assertTrue(user.isJastiper());
    }

    @Test
    public void testIsJastiperFalse() {
        User titiper = new User();
        titiper.setRole(Role.ROLE_TITIPER.toString());

        User admin = new User();
        admin.setRole(Role.ROLE_ADMIN.toString());

        assertFalse(titiper.isJastiper());
        assertFalse(admin.isJastiper());
    }

    @Test
    public void testGetAuthorities() {
        User user = new User();
        user.setRole("ROLE_ADMIN");

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(1, authorities.size());

        GrantedAuthority authority = authorities.iterator().next();
        assertEquals("ROLE_ADMIN", authority.getAuthority());
    }

    @Test
    public void testIsEnabledBanned() {
        User banned = new User();
        banned.setStatus(Status.BANNED.toString());

        assertFalse(banned.isEnabled());
    }

    @Test
    public void testIsEnabledNotBanned() {
        User active = new User();
        active.setStatus(Status.ACTIVE.toString());

        User inactive = new User();
        inactive.setStatus(Status.INACTIVE.toString());

        User pending = new User();
        pending.setStatus(Status.PENDING.toString());

        assertTrue(active.isEnabled());
        assertTrue(inactive.isEnabled());
        assertTrue(pending.isEnabled());
    }

    @Test
    public void testGetSuccessRate() {
        User titiper = new User();
        titiper.setRole(Role.ROLE_TITIPER.toString());

        User jastiper0TriedToSell = new User();
        jastiper0TriedToSell.setRole(Role.ROLE_JASTIPER.toString());
        jastiper0TriedToSell.setTried_to_sell(0);
        jastiper0TriedToSell.setSuccessfully_sold(0);

        User jastiperHasTriedToSell = new User();
        jastiperHasTriedToSell.setRole(Role.ROLE_JASTIPER.toString());
        jastiperHasTriedToSell.setTried_to_sell(4);
        jastiperHasTriedToSell.setSuccessfully_sold(3);

        User admin = new User();
        admin.setRole(Role.ROLE_ADMIN.toString());

        assertTrue(titiper.getSuccessRate() < 0.001);
        assertTrue(admin.getSuccessRate() < 0.001);
        assertTrue(jastiper0TriedToSell.getSuccessRate() < 0.001);
        assertTrue(jastiperHasTriedToSell.getSuccessRate() > 0.74 &&
                jastiperHasTriedToSell.getSuccessRate() < 0.76);
    }
}
