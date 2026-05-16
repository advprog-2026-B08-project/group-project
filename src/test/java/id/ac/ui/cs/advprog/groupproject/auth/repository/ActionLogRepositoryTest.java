package id.ac.ui.cs.advprog.groupproject.auth.repository;

import id.ac.ui.cs.advprog.groupproject.auth.model.ActionLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ActionLogRepositoryTest {

    @Autowired
    private ActionLogRepository actionLogRepository;

    @Test
    void findAllByOrderByTimestampDescShouldSortCorrectly() {
        ActionLog older = new ActionLog();
        older.setAction("OLD");
        older.setTimestamp(LocalDateTime.now().minusDays(1));

        ActionLog newer = new ActionLog();
        newer.setAction("NEW");
        newer.setTimestamp(LocalDateTime.now());

        actionLogRepository.save(older);
        actionLogRepository.save(newer);

        List<ActionLog> result = actionLogRepository.findAllByOrderByTimestampDesc();

        assertEquals(2, result.size());
        assertEquals("NEW", result.get(0).getAction());
        assertEquals("OLD", result.get(1).getAction());
    }
}