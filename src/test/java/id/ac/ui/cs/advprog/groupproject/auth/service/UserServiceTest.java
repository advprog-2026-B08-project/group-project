package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.LogType;
import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.groupproject.event.UserRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ActionLogService logService;

    @InjectMocks
    private CustomUserDetailService userDetailService;

    @Test
    void testLoadUserByUsername() {
        User user = new User();
        user.setEmail("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailService.loadUserByUsername("test@gmail.com");

        assertEquals(user, result);
        verify(userRepository).findByEmail("test@gmail.com");
    }

    @Test
    void testLoadUserByUsernameNotFound() {

        when(userRepository.findByEmail("missing@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailService.loadUserByUsername("missing@gmail.com")
        );
    }

    @Test
    void testEmailExists() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(new User()));
        boolean result = userDetailService.emailExists("test@gmail.com");
        assertTrue(result);
    }

    @Test
    void testEmailExistsNotFound() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        boolean result = userDetailService.emailExists("test@gmail.com");
        assertFalse(result);
    }

    @Test
    void testConfirmPasswordTrue() {
        assertTrue(userDetailService.confirmPassword("abc", "abc"));
    }

    @Test
    void testConfirmPasswordFalse() {
        assertFalse(userDetailService.confirmPassword("abc", "aaa"));
    }

    @Test
    void testCreateUser() {
        when(passwordEncoder.encode("password")).thenReturn("ENCODED");

        User result = userDetailService.createUser(
                "a@gmail.com",
                "password",
                "a",
                "a a ron"
        );

        assertEquals("a", result.getUsername());
        assertEquals("ENCODED", result.getPassword());
        assertEquals(Role.ROLE_TITIPER.toString(), result.getRole());
        assertEquals(Status.ACTIVE.toString(), result.getStatus());
        assertEquals("a@gmail.com", result.getEmail());
        assertEquals("a a ron", result.getFullName());

        verify(userRepository).save(result);
        verify(passwordEncoder).encode("password");
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
        verify(logService).log(
                eq("Registered a new user"),
                eq("a"),
                eq(Role.ROLE_TITIPER.toString()),
                eq(null),
                contains("created a new account"),
                eq(LogType.INFO)
        );
    }

    @Test
    void testCreateUserWithDefaultUsername() {
        when(passwordEncoder.encode(any())).thenReturn("ENCODED");

        User user = userDetailService.createUser(
                "a@gmail.com",
                "pass",
                "",
                "a a ron"
        );

        assertEquals("a", user.getUsername());
    }

    @Test
    void testGetDefaultUsername() {
        String result = userDetailService.getDefaultUsername("abc@gmail.com");
        assertEquals("abc", result);
    }

    @Test
    void testGetUserCountByRole() {
        List<Object[]> rows = List.of(
                new Object[]{"ROLE_ADMIN", 1L},
                new Object[]{"ROLE_TITIPER", 2L}
        );

        when(userRepository.countUsersByRole()).thenReturn(rows);

        Map<String, Long> result = userDetailService.getUserCountByRole();

        assertEquals(1L, result.get("ROLE_ADMIN"));
        assertEquals(2L, result.get("ROLE_TITIPER"));
    }

    @Test
    void testGetUserList() {
        List<User> users = List.of(
                new User(),
                new User()
        );

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userDetailService.getUserList();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void testDemoteSuccess() {
        UUID id = UUID.randomUUID();

        User admin = new User();
        admin.setRole("ROLE_ADMIN");
        admin.setUsername("admin");

        User user = new User();
        user.setRole("ROLE_JASTIPER");
        user.setUsername("user");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userDetailService.demote(admin, id);

        assertEquals(Role.ROLE_TITIPER.toString(), user.getRole());
        verify(userRepository).save(user);
        verify(logService).log(
                eq("Demoted a user"),
                eq("admin"),
                eq("ROLE_ADMIN"),
                eq("user"),
                contains("demoted"),
                eq(LogType.WARN)
        );
    }

    @Test
    void testDemoteUnauthorized() {
        UUID id = UUID.randomUUID();

        User fakeAdmin = new User();
        fakeAdmin.setRole("ROLE_TITIPER");
        fakeAdmin.setUsername("user");

        User target = new User();

        when(userRepository.findById(id)).thenReturn(Optional.of(target));

        userDetailService.demote(fakeAdmin, id);

        verify(logService).log(
                eq("Unauthorized action"),
                eq("user"),
                eq("ROLE_TITIPER"),
                eq(null),
                contains("unauthorized"),
                eq(LogType.DANGER)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void testBanSuccess() {
        UUID id = UUID.randomUUID();

        User admin = new User();
        admin.setRole("ROLE_ADMIN");
        admin.setUsername("admin");

        User user = new User();
        user.setRole("ROLE_TITIPER");
        user.setUsername("user");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userDetailService.ban(admin, id);

        assertEquals(Role.ROLE_TITIPER.toString(), user.getRole());
        verify(userRepository).save(user);
        verify(logService).log(
                eq("Banned a user"),
                eq("admin"),
                eq("ROLE_ADMIN"),
                eq("user"),
                contains("banned"),
                eq(LogType.WARN)
        );
    }

    @Test
    void testBanUnauthorized() {
        UUID id = UUID.randomUUID();

        User fakeAdmin = new User();
        fakeAdmin.setRole("ROLE_TITIPER");
        fakeAdmin.setUsername("user");

        User target = new User();

        when(userRepository.findById(id)).thenReturn(Optional.of(target));

        userDetailService.ban(fakeAdmin, id);

        verify(logService).log(
                eq("Unauthorized action"),
                eq("user"),
                eq("ROLE_TITIPER"),
                eq(null),
                contains("unauthorized"),
                eq(LogType.DANGER)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void testLiftBanSuccess() {
        UUID id = UUID.randomUUID();

        User admin = new User();
        admin.setRole("ROLE_ADMIN");
        admin.setUsername("admin");
        admin.setStatus(Status.ACTIVE.toString());

        User user = new User();
        user.setRole("ROLE_TITIPER");
        user.setUsername("user");
        user.setStatus(Status.BANNED.toString());

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userDetailService.liftBan(admin, id);

        assertEquals(Role.ROLE_TITIPER.toString(), user.getRole());
        verify(userRepository).save(user);
        verify(logService).log(
                eq("lifted a ban"),
                eq("admin"),
                eq("ROLE_ADMIN"),
                eq("user"),
                contains("lifted the ban"),
                eq(LogType.WARN)
        );
    }

    @Test
    void testLiftBanUnauthorized() {
        UUID id = UUID.randomUUID();

        User fakeAdmin = new User();
        fakeAdmin.setRole("ROLE_TITIPER");
        fakeAdmin.setUsername("user");

        User target = new User();

        when(userRepository.findById(id)).thenReturn(Optional.of(target));

        userDetailService.liftBan(fakeAdmin, id);

        verify(logService).log(
                eq("Unauthorized action"),
                eq("user"),
                eq("ROLE_TITIPER"),
                eq(null),
                contains("unauthorized"),
                eq(LogType.DANGER)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateProfile() {
        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);
        user.setRole(Role.ROLE_TITIPER.toString());

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userDetailService.updateProfile(
                id,
                "a",
                "https://smth-smth",
                "a a ron",
                "https://image"
        );

        assertEquals("a", user.getUsername());
        assertEquals("https://smth-smth", user.getSocials());
        assertEquals("a a ron", user.getFullName());
        assertEquals("https://image", user.getProfilePictureURL());

        verify(userRepository).save(user);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        assertEquals(user, auth.getPrincipal());
    }

    @Test
    void testUpdateProfileWithBlanks() {
        UUID id = UUID.randomUUID();

        User user = new User();
        user.setRole(Role.ROLE_TITIPER.toString());
        user.setSocials("https://smth-smth");
        user.setFullName("a a ron");
        user.setProfilePictureURL("https://image");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userDetailService.updateProfile(
                id,
                "a",
                "",
                "",
                ""
        );

        assertEquals("https://smth-smth", user.getSocials());
        assertEquals("a a ron", user.getFullName());
        assertEquals("https://image", user.getProfilePictureURL());
    }
}
