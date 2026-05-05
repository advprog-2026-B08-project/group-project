package id.ac.ui.cs.advprog.groupproject.wallet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WalletPageController {

    @GetMapping("/wallet")
    public String walletPage() {
        return "wallet/index";
    }

    @GetMapping("/admin/wallet")
    public String walletAdminPage() {
        return "wallet/admin";
    }
}
