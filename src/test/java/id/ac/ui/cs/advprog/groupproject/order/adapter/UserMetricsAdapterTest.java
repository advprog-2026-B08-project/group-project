package id.ac.ui.cs.advprog.groupproject.order.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserMetricsAdapterTest {

    @Mock
    private UserRepository userRepository;

    private UserMetricsAdapter userMetricsAdapter;
    private UUID jastiperId;
    private User jastiperUser;

    @BeforeEach
    void setUp() {
        userMetricsAdapter = new UserMetricsAdapter(userRepository);
        jastiperId = UUID.randomUUID();
        jastiperUser = new User();
        jastiperUser.setId(jastiperId);
        jastiperUser.setTriedToSell(0);
        jastiperUser.setSuccessfullySold(0);
    }

    @Test
    void incrementTriedToSell_success() {
        when(userRepository.findById(jastiperId)).thenReturn(Optional.of(jastiperUser));
        
        userMetricsAdapter.incrementTriedToSell(jastiperId);
        
        assertEquals(1, jastiperUser.getTriedToSell());
        verify(userRepository).save(jastiperUser);
    }

    @Test
    void incrementTriedToSell_userNotFound() {
        when(userRepository.findById(jastiperId)).thenReturn(Optional.empty());
        
        assertDoesNotThrow(() -> userMetricsAdapter.incrementTriedToSell(jastiperId));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void incrementTriedToSell_exceptionCaught() {
        when(userRepository.findById(jastiperId)).thenThrow(new RuntimeException("DB error"));
        
        assertDoesNotThrow(() -> userMetricsAdapter.incrementTriedToSell(jastiperId));
    }

    @Test
    void incrementSuccessfullySold_success() {
        when(userRepository.findById(jastiperId)).thenReturn(Optional.of(jastiperUser));
        
        userMetricsAdapter.incrementSuccessfullySold(jastiperId);
        
        assertEquals(1, jastiperUser.getSuccessfullySold());
        verify(userRepository).save(jastiperUser);
    }

    @Test
    void incrementSuccessfullySold_userNotFound() {
        when(userRepository.findById(jastiperId)).thenReturn(Optional.empty());
        
        assertDoesNotThrow(() -> userMetricsAdapter.incrementSuccessfullySold(jastiperId));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void incrementSuccessfullySold_exceptionCaught() {
        when(userRepository.findById(jastiperId)).thenThrow(new RuntimeException("DB error"));
        
        assertDoesNotThrow(() -> userMetricsAdapter.incrementSuccessfullySold(jastiperId));
    }
}
