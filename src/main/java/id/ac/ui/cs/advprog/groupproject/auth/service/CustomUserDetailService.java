package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.exception.AuthOperationException;
import id.ac.ui.cs.advprog.groupproject.auth.model.LogType;
import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;

import id.ac.ui.cs.advprog.groupproject.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomUserDetailService.class);

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_JASTIPER = "ROLE_JASTIPER";
    private static final String ROLE_TITIPER = "ROLE_TITIPER";

    private static final String LOG_ACTION_UNAUTHORIZED = "Unauthorized action";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found!";
    private static final String ACCOUNT_SUFFIX = "'s account";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final ActionLogService logService;

    public CustomUserDetailService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   ApplicationEventPublisher eventPublisher,
                                   ActionLogService logService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.logService = logService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            LOGGER.warn("auth_login_failed reason=user_not_found email={}", email);
            return new UsernameNotFoundException("User not found");
        });
        LOGGER.debug("auth_user_loaded userId={} role={}", user.getId(), user.getRole());
        return user;
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean confirmPassword(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public User createUser(String email, String password, String username, String fullName) {
        String usernameInput = username;
        if (usernameInput == null || usernameInput.isBlank()) {
            usernameInput = getDefaultUsername(email);
        }

        User user = new User();
        user.setUsername(usernameInput);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ROLE_TITIPER.toString());
        user.setStatus(Status.ACTIVE.toString());
        user.setEmail(email);
        if (fullName != null) {
            user.setFullName(fullName);
        }

        userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(this, user.getId()));

        LOGGER.info("auth_user_created userId={} username={} role={}",
                user.getId(), user.getUsername(), user.getRole());

        String description = user.getUsername() + " created a new account!";
        logService.log("Registered a new user", user.getUsername(),
                user.getRole(), null, description, LogType.INFO);
        return user;
    }

    public String getDefaultUsername(String email) {
        return email.split("@")[0];
    }

    public Map<String, Long> getUserCountByRole() {
        List<Object[]> results = userRepository.countUsersByRole();
        Map<String, Long> map = new HashMap<>();

        for (Object[] row : results) {
            String role = (String) row[0];
            Long count = (Long) row[1];
            map.put(role, count);
        }
        return map;
    }

    public Page<User> getFilteredUsers(User currentUser, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (currentUser.getRole().equals(ROLE_ADMIN)) {
            if (isKnownRole(role)) {
                return userRepository.findByRole(role, pageable);
            }
            return userRepository.findAll(pageable);
        }

        if (role != null && (role.equals(ROLE_JASTIPER) || role.equals(ROLE_TITIPER))) {
            return userRepository.findByRole(role, pageable);
        }
        return userRepository.findByRoleNot(ROLE_ADMIN, pageable);
    }

    private boolean isKnownRole(String role) {
        return role != null
                && (role.equals(ROLE_ADMIN) || role.equals(ROLE_JASTIPER) || role.equals(ROLE_TITIPER));
    }

    public void demote(User admin, UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthOperationException("User not found"));
        if (!admin.getRole().equals(ROLE_ADMIN)) {
            String description = admin.getUsername()
                    + "tried to perform an unauthorized action of demoting another user";
            logService.log(LOG_ACTION_UNAUTHORIZED, admin.getUsername(),
                    admin.getRole(), null, description, LogType.DANGER);
            return;
        }
        if (!user.getRole().equals(ROLE_JASTIPER)) {
            throw new AuthOperationException("Invalid role for demotion!");
        }

        user.setRole(Role.ROLE_TITIPER.toString());
        userRepository.save(user);

        String description = admin.getUsername()
                + " demoted "
                + user.getUsername()
                + ACCOUNT_SUFFIX;

        logService.log("Demoted a user", admin.getUsername(),
                admin.getRole(), user.getUsername(), description, LogType.WARN);
    }

    public void ban(User admin, UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthOperationException(USER_NOT_FOUND_MESSAGE));
        if (!admin.getRole().equals(ROLE_ADMIN)) {
            String description = admin.getUsername()
                    + "tried to perform an unauthorized action of banning another user";
            logService.log(LOG_ACTION_UNAUTHORIZED, admin.getUsername(),
                    admin.getRole(), null, description, LogType.DANGER);
            return;
        }
        if (user.getRole().equals(ROLE_ADMIN)) {
            throw new AuthOperationException("Can't ban an admin!");
        }

        user.setStatus(Status.BANNED.toString());
        userRepository.save(user);

        String description = admin.getUsername()
                + " banned "
                + user.getUsername()
                + ACCOUNT_SUFFIX;

        logService.log("Banned a user", admin.getUsername(),
                admin.getRole(), user.getUsername(), description, LogType.WARN);
    }

    public void liftBan(User admin, UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthOperationException(USER_NOT_FOUND_MESSAGE));
        if (!admin.getRole().equals(ROLE_ADMIN)) {
            String description = admin.getUsername()
                    + "tried to perform an unauthorized action of lifting the ban off another user";
            logService.log(LOG_ACTION_UNAUTHORIZED, admin.getUsername(),
                    admin.getRole(), null, description, LogType.DANGER);
            return;
        }
        if (user.getRole().equals(ROLE_ADMIN)) {
            throw new AuthOperationException("Account can't be banned!");
        }
        if (!user.getStatus().equals(Status.BANNED.toString())) {
            throw new AuthOperationException("User was not banned in the first place");
        }

        user.setStatus(Status.ACTIVE.toString());
        userRepository.save(user);

        String description = admin.getUsername()
                + " lifted the ban from "
                + user.getUsername()
                + ACCOUNT_SUFFIX;

        logService.log("lifted a ban", admin.getUsername(),
                admin.getRole(), user.getUsername(), description, LogType.WARN);
    }

    public void updateProfile(UUID id, String username, String socials, String fullName, String profilePictureURL) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthOperationException(USER_NOT_FOUND_MESSAGE));

        user.setUsername(username);

        if (socials != null && !socials.isBlank()) {
            user.setSocials(socials);
        }
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (profilePictureURL != null && !profilePictureURL.isBlank()) {
            user.setProfilePictureURL(profilePictureURL);
        }
        userRepository.save(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
