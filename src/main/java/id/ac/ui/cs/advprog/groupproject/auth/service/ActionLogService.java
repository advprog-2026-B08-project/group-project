package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.ActionLog;
import id.ac.ui.cs.advprog.groupproject.auth.repository.ActionLogRepository;

import java.time.LocalDateTime;

public class ActionLogService {
    ActionLogRepository logRepository;

    public ActionLogService(ActionLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void log(String action, String actor, String actorRole, String target, String description) {
        ActionLog log = new ActionLog();
        log.setAction(action);
        log.setActor(actor);
        log.setActorRole(actorRole);
        log.setTarget(target);
        log.setDescription(description);
        log.setTimestamp(LocalDateTime.now());

        logRepository.save(log);
    }
}
