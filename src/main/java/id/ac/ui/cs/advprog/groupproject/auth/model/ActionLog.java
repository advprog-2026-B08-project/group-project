package id.ac.ui.cs.advprog.groupproject.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Getter @Setter
@Table(name = "activity_log")
public class ActionLog {
    @GeneratedValue @Id
    UUID id;

    @Column(name = "action")
    private String action;
    @Column(name = "actor")
    private String actor;
    @Column(name = "actor_role")
    private String actorRole;
    @Column(name = "target")
    private String target;
    @Column(name = "description")
    private String description;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    @Column(name = "log_type")
    @Enumerated(EnumType.STRING)
    private LogType logType;
}
