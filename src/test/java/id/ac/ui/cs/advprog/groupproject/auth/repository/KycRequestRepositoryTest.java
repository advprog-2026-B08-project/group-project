package id.ac.ui.cs.advprog.groupproject.auth.repository;

import id.ac.ui.cs.advprog.groupproject.auth.model.KycRequest;
import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class KycRequestRepositoryTest {
    @Autowired
    private KycRequestRepository kycRequestRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByUserShouldReturnCorrectRequest() {

        User user = new User();
        user.setUsername("a");
        user.setEmail("a@gmail.com");
        user.setPassword("a");
        user.setRole(Role.ROLE_TITIPER.toString());
        user.setStatus(Status.ACTIVE.toString());

        entityManager.persist(user);

        KycRequest request = new KycRequest();
        request.setUser(user);

        kycRequestRepository.save(request);

        Optional<KycRequest> result = kycRequestRepository.findByUser(user);

        assertTrue(result.isPresent());
        assertEquals(user.getUsername(), result.get().getUser().getUsername());
    }

    @Test
    void testCountRequestByStatus() {

        KycRequest r1 = new KycRequest();
        r1.setStatus(Status.ACTIVE);

        KycRequest r2 = new KycRequest();
        r2.setStatus(Status.ACTIVE);

        KycRequest r3 = new KycRequest();
        r3.setStatus(Status.INACTIVE);

        kycRequestRepository.save(r1);
        kycRequestRepository.save(r2);
        kycRequestRepository.save(r3);

        List<Object[]> result = kycRequestRepository.countRequestByStatus();
        assertEquals(2, result.size());
    }

    @Test
    void testGetPendingRequests() {

        KycRequest active = new KycRequest();
        active.setStatus(Status.ACTIVE);

        KycRequest rejected = new KycRequest();
        rejected.setStatus(Status.INACTIVE);

        kycRequestRepository.save(active);
        kycRequestRepository.save(rejected);

        List<KycRequest> result = kycRequestRepository.getPendingRequests();
        assertEquals(1, result.size());

        assertEquals(Status.ACTIVE, result.get(0).getStatus());
    }
}
