package id.ac.ui.cs.advprog.groupproject.auth.repository;

import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail() {
        User user1= new User();
        user1.setEmail("a@gmail.com");
        user1.setPassword("a");
        user1.setUsername("a");
        user1.setStatus(Status.ACTIVE.toString());
        user1.setRole(Role.ROLE_TITIPER.toString());
        userRepository.save(user1);

        assertTrue(userRepository.findByEmail("a@gmail.com").isPresent());
        assertEquals(user1, userRepository.findByEmail("a@gmail.com").get());

        assertFalse(userRepository.findByEmail("b@gmail.com").isPresent());
    }

    @Test
    void testFindByUsername() {
        User user1= new User();
        user1.setEmail("1@gmail.com");
        user1.setUsername("a");
        user1.setPassword("a");
        user1.setUsername("a");
        user1.setStatus(Status.ACTIVE.toString());
        user1.setRole(Role.ROLE_TITIPER.toString());
        userRepository.save(user1);

        assertTrue(userRepository.findByUsername("a").isPresent());
        assertEquals(user1, userRepository.findByUsername("a").get());

        assertFalse(userRepository.findByUsername("b").isPresent());
    }

    @Test
    void countUsersByRoleShouldCountCorrectly() {

        User user1 = new User();
        user1.setPassword("a");
        user1.setUsername("a");
        user1.setEmail("a@gmail.com");
        user1.setStatus(Status.ACTIVE.toString());
        user1.setRole(Role.ROLE_TITIPER.toString());

        User user2 = new User();
        user2.setPassword("b");
        user2.setUsername("b");
        user2.setEmail("b@gmail.com");
        user2.setStatus(Status.ACTIVE.toString());
        user2.setRole(Role.ROLE_TITIPER.toString());


        User user3 = new User();
        user3.setPassword("c");
        user3.setUsername("c");
        user3.setEmail("c@gmail.com");
        user3.setStatus(Status.ACTIVE.toString());
        user3.setRole(Role.ROLE_JASTIPER.toString());

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        List<Object[]> result =
                userRepository.countUsersByRole();

        for (Object[] row : result) {

            String role = (String) row[0];
            Long count = (Long) row[1];

            if (role.equals("ROLE_ADMIN")) {
                assertEquals(2L, count);
            }

            if (role.equals("ROLE_JASTIPER")) {
                assertEquals(1L, count);
            }
        }
    }

    @Test
    void testFindAllPageable() {

        User user1 = new User();
        user1.setUsername("a");
        user1.setEmail("a@gmail.com");
        user1.setPassword("a");
        user1.setRole(Role.ROLE_ADMIN.toString());
        user1.setStatus(Status.ACTIVE.toString());

        User user2 = new User();
        user2.setUsername("b");
        user2.setEmail("b@gmail.com");
        user2.setPassword("b");
        user2.setRole(Role.ROLE_JASTIPER.toString());
        user2.setStatus(Status.ACTIVE.toString());

        User user3 = new User();
        user3.setUsername("c");
        user3.setEmail("c@gmail.com");
        user3.setPassword("c");
        user3.setRole(Role.ROLE_TITIPER.toString());
        user3.setStatus(Status.ACTIVE.toString());

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        Pageable pageable = PageRequest.of(0, 2);

        Page<User> result = userRepository.findAll(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
    }

    @Test
    void testFindByRole() {

        User user1 = new User();
        user1.setUsername("jastiper");
        user1.setEmail("jastiper@gmail.com");
        user1.setPassword("a");
        user1.setRole(Role.ROLE_JASTIPER.toString());
        user1.setStatus(Status.ACTIVE.toString());

        User user2 = new User();
        user2.setUsername("titiper");
        user2.setEmail("titiper@gmail.com");
        user2.setPassword("b");
        user2.setRole(Role.ROLE_TITIPER.toString());
        user2.setStatus(Status.ACTIVE.toString());

        userRepository.save(user1);
        userRepository.save(user2);

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result =
                userRepository.findByRole(
                        Role.ROLE_JASTIPER.toString(),
                        pageable
                );

        assertEquals(1, result.getContent().size());
        assertEquals(
                "jastiper",
                result.getContent().get(0).getUsername()
        );
    }

    @Test
    void testFindByRoleNot() {

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword("a");
        admin.setRole(Role.ROLE_ADMIN.toString());
        admin.setStatus(Status.ACTIVE.toString());

        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@gmail.com");
        user1.setPassword("b");
        user1.setRole(Role.ROLE_TITIPER.toString());
        user1.setStatus(Status.ACTIVE.toString());

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@gmail.com");
        user2.setPassword("c");
        user2.setRole(Role.ROLE_JASTIPER.toString());
        user2.setStatus(Status.ACTIVE.toString());

        userRepository.save(admin);
        userRepository.save(user1);
        userRepository.save(user2);

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result =
                userRepository.findByRoleNot(
                        Role.ROLE_ADMIN.toString(),
                        pageable
                );

        assertEquals(2, result.getContent().size());

        assertTrue(
                result.getContent()
                        .stream()
                        .noneMatch(user ->
                                user.getRole().equals(
                                        Role.ROLE_ADMIN.toString()
                                )
                        )
        );
    }
}
