package id.ac.ui.cs.advprog.groupproject.config;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UserGlobalAccess {
    @ModelAttribute("user")
    public User addUserToModel(@AuthenticationPrincipal User user) {
        System.out.println("\n User : " + user);
        return user;
    }
}