package id.ac.ui.cs.advprog.groupproject.wallet.event;

import id.ac.ui.cs.advprog.groupproject.event.UserRegisteredEvent;
import id.ac.ui.cs.advprog.groupproject.wallet.service.WalletService;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class WalletEventListener {
    private final WalletService walletService;
    public WalletEventListener(WalletService walletService) {
        this.walletService = walletService;
    }
    
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        walletService.createWallet(event.getUserId());
    }
}