package id.ac.ui.cs.advprog.groupproject.auth.repository;

import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByEmail() {
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
    public void testFindByUsername() {
        User user1= new User();
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
        user1.setStatus(Status.ACTIVE.toString());
        user1.setRole(Role.ROLE_TITIPER.toString());

        User user2 = new User();
        user2.setPassword("b");
        user2.setUsername("b");
        user2.setStatus(Status.ACTIVE.toString());
        user2.setRole(Role.ROLE_TITIPER.toString());


        User user3 = new User();
        user3.setPassword("c");
        user3.setUsername("c");
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
}
