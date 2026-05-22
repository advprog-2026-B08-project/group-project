package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.ActionLog;
import id.ac.ui.cs.advprog.groupproject.auth.model.LogType;
import id.ac.ui.cs.advprog.groupproject.auth.repository.ActionLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionLogServiceTest {
    @Mock
    ActionLogRepository logRepository;

    @InjectMocks
    ActionLogService logService;

    @Test
    void testGetAllLogs() {
        List<ActionLog> list = List.of(
                new ActionLog(),
                new ActionLog()
        );

        when(logRepository.findAllByOrderByTimestampDesc()).thenReturn(list);

        List<ActionLog> result = logService.getAllLogs();

        assertEquals(2, result.size());
        verify(logRepository).findAllByOrderByTimestampDesc();
    }

    @Test
    void testLog() {
        ActionLog log = new ActionLog();
        log.setLogType(LogType.INFO);
        log.setAction("placeholder");
        log.setTarget("user");
        log.setTimestamp(LocalDateTime.now());
        log.setDescription("placeholder");
        log.setActorRole("admin");
        log.setActor("admin");
        log.setId(UUID.randomUUID());

        logService.log("placeholder", "admin", "admin", "user", "placeholder", LogType.INFO);
        ArgumentCaptor<ActionLog> captor = ArgumentCaptor.forClass(ActionLog.class);

        verify(logRepository).save(captor.capture());

        ActionLog savedLog = captor.getValue();

        assertEquals("placeholder", savedLog.getAction());
        assertEquals("admin", savedLog.getActor());
        assertEquals("admin", savedLog.getActorRole());
        assertEquals("user", savedLog.getTarget());
        assertEquals("placeholder", savedLog.getDescription());
        assertEquals(LogType.INFO, savedLog.getLogType());

        assertNotNull(savedLog.getTimestamp());
    }
}
