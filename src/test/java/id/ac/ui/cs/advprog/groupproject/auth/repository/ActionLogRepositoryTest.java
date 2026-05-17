package id.ac.ui.cs.advprog.groupproject.auth.repository;

import id.ac.ui.cs.advprog.groupproject.auth.model.ActionLog;
import id.ac.ui.cs.advprog.groupproject.auth.model.LogType;
import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
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
    void testFindAllByOrderByTimestampDesc() {
        ActionLog older = new ActionLog();
        older.setAction("log 1");
        older.setActor("admin");
        older.setActorRole(Role.ROLE_ADMIN.toString());
        older.setLogType(LogType.INFO);
        older.setTimestamp(LocalDateTime.now().minusDays(1));

        ActionLog newer = new ActionLog();
        newer.setAction("log 2");
        newer.setActor("admin");
        newer.setActorRole(Role.ROLE_ADMIN.toString());
        newer.setLogType(LogType.INFO);
        newer.setTimestamp(LocalDateTime.now());

        actionLogRepository.save(older);
        actionLogRepository.save(newer);

        List<ActionLog> result = actionLogRepository.findAllByOrderByTimestampDesc();

        assertEquals(2, result.size());
        assertEquals("log 2", result.get(0).getAction());
        assertEquals("log 1", result.get(1).getAction());
    }
}