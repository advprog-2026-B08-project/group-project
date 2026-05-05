package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.ActionLog;
import id.ac.ui.cs.advprog.groupproject.auth.model.LogType;
import id.ac.ui.cs.advprog.groupproject.auth.repository.ActionLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActionLogService {
    ActionLogRepository logRepository;

    public ActionLogService(ActionLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public List<ActionLog> getAllLogs() {
        return logRepository.findAllByOrderByTimestampDesc();
    }

    public void log(String action, String actor, String actorRole,
                    String target, String description, LogType type) {
        ActionLog log = new ActionLog();
        log.setAction(action);
        log.setActor(actor);
        log.setActorRole(actorRole);
        log.setTarget(target);
        log.setDescription(description);
        log.setTimestamp(LocalDateTime.now());
        log.setLogType(type);

        logRepository.save(log);
    }
}
