package id.ac.ui.cs.advprog.groupproject.event;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class UserRegisteredEvent extends ApplicationEvent{
    private final UUID userId;
    public UserRegisteredEvent(Object source, UUID userId) {
        super(source);
        this.userId = userId;
    }
}
