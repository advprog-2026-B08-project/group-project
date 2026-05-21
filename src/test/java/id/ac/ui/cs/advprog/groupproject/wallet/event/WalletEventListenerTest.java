package id.ac.ui.cs.advprog.groupproject.wallet.event;

import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.groupproject.event.UserRegisteredEvent;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
class WalletEventListenerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletEventListener walletEventListener;

    @Test
    void handleUserRegistered_CreatesWallet() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(this, userId);

        walletEventListener.handleUserRegistered(event);

        verify(walletService).createWallet(userId);
    }
}
