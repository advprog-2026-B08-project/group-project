package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.ActionLog;
import id.ac.ui.cs.advprog.groupproject.auth.model.LogType;
import id.ac.ui.cs.advprog.groupproject.auth.repository.ActionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActionLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionLogService.class);

    private final ActionLogRepository logRepository;

    public ActionLogService(ActionLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public List<ActionLog> getAllLogs() {
        return logRepository.findAllByOrderByTimestampDesc();
    }

    @Async("auditLogExecutor")
    public void log(String action, String actor, String actorRole,
                    String target, String description, LogType type) {
        try {
            ActionLog log = new ActionLog();
            log.setAction(action);
            log.setActor(actor);
            log.setActorRole(actorRole);
            log.setTarget(target);
            log.setDescription(description);
            log.setTimestamp(LocalDateTime.now());
            log.setLogType(type);

            logRepository.save(log);
        } catch (Exception ex) {
            LOGGER.warn("audit_log_persist_failed action={} actor={} target={} reason={}",
                    action, actor, target, ex.getMessage());
        }
    }
}
