package id.ac.ui.cs.advprog.groupproject.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity @Getter @Setter
public class KycRequest {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private Role requestedRole;

    @Enumerated(EnumType.STRING)
    private Status status;
}
