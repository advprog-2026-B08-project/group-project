package id.ac.ui.cs.advprog.groupproject.auth.repository;

import id.ac.ui.cs.advprog.groupproject.auth.model.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActionLogRepository extends JpaRepository<ActionLog, UUID> {
    List<ActionLog> findAllByOrderByTimestampDesc();
}
