package id.ac.ui.cs.advprog.groupproject.auth.controller;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.service.CustomUserDetailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {
    @Mock
    private CustomUserDetailService userDetailService;

    @InjectMocks
    private ProfileController profileController;

    @Test
    void testProfile() {
        User user = new User();
        Model model = new ExtendedModelMap();

        String view = profileController.profile(user, model);

        assertEquals("auth/profile/profile", view);
        assertTrue(model.containsAttribute("user"));
    }

    @Test
    void testProfileEdit() {
        User user = new User();
        Model model = new ExtendedModelMap();

        String view = profileController.profileEdit(user, model);

        assertEquals("auth/profile/profileEdit", view);
        assertTrue(model.containsAttribute("user"));
    }

    @Test
    void testProfileUpdate() {
        User user = new User();
        String username = "a";
        String socials = "https://smth-smth";
        String fullName = "a a ron";
        String profilePictureURL = "https://randomBsGo";

        doNothing().when(userDetailService).updateProfile(user.getId(), username, socials, fullName, profilePictureURL);
        String view = profileController.updateProfile(user, username, socials, fullName, profilePictureURL);

        assertEquals("redirect:/profile", view);
    }
}
