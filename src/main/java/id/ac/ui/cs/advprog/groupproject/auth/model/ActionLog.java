package id.ac.ui.cs.advprog.groupproject.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Getter @Setter
public class ActionLog {
    @GeneratedValue @Id
    UUID id;

    private String action;
    private String actor;
    private String actorRole;
    private String target;
    private String description;
    private LocalDateTime timestamp;

}
