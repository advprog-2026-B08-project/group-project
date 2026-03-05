package id.ac.ui.cs.advprog.groupproject.repository;

import id.ac.ui.cs.advprog.groupproject.model.Catalog;
import id.ac.ui.cs.advprog.groupproject.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class CatalogRepositoryTest {

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRole("JASTIPER");
        testUser = userRepository.save(testUser);

        anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setPassword("password");
        anotherUser.setRole("JASTIPER");
        anotherUser = userRepository.save(anotherUser);
    }

    @Test
    void testFindByJastiperId_MultipleResults() {
        Catalog catalog1 = new Catalog();
        catalog1.setName("Product 1");
        catalog1.setPrice(100.0);
        catalog1.setStock(10);
        catalog1.setOriginLocation("Jakarta");
        catalog1.setTravelDate(LocalDate.now().plusDays(7));
        catalog1.setJastiper(testUser);
        catalogRepository.save(catalog1);

        Catalog catalog2 = new Catalog();
        catalog2.setName("Product 2");
        catalog2.setPrice(200.0);
        catalog2.setStock(20);
        catalog2.setOriginLocation("Bandung");
        catalog2.setTravelDate(LocalDate.now().plusDays(14));
        catalog2.setJastiper(testUser);
        catalogRepository.save(catalog2);

        Catalog catalog3 = new Catalog();
        catalog3.setName("Product 3");
        catalog3.setPrice(300.0);
        catalog3.setStock(30);
        catalog3.setOriginLocation("Surabaya");
        catalog3.setTravelDate(LocalDate.now().plusDays(21));
        catalog3.setJastiper(anotherUser);
        catalogRepository.save(catalog3);

        List<Catalog> testUserCatalogs = catalogRepository.findByJastiperId(testUser.getId());
        List<Catalog> anotherUserCatalogs = catalogRepository.findByJastiperId(anotherUser.getId());

        assertEquals(2, testUserCatalogs.size());
        assertEquals(1, anotherUserCatalogs.size());
        assertTrue(testUserCatalogs.stream().allMatch(c -> c.getJastiper().getId().equals(testUser.getId())));
    }

    @Test
    void testFindByJastiperId_EmptyResult() {
        UUID nonExistentId = UUID.randomUUID();
        List<Catalog> catalogs = catalogRepository.findByJastiperId(nonExistentId);

        assertNotNull(catalogs);
        assertTrue(catalogs.isEmpty());
    }
}
