package id.ac.ui.cs.advprog.groupproject.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.groupproject.service.WalletService;

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