package id.ac.ui.cs.advprog.groupproject.auth.repository;

import id.ac.ui.cs.advprog.groupproject.auth.model.KycRequest;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KycRequestRepository extends JpaRepository<KycRequest, UUID> {
    Optional<KycRequest> findByUser(User user);

    @Query("SELECT r.status, COUNT(r) from KycRequest r GROUP BY r.status")
    List<Object[]> countRequestByStatus();
}
