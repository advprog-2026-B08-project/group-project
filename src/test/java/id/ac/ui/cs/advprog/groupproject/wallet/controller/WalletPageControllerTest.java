package id.ac.ui.cs.advprog.groupproject.wallet.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WalletPageControllerTest {

    @Test
    void walletPage_ReturnsView() {
        WalletPageController controller = new WalletPageController();

        assertEquals("wallet/index", controller.walletPage());
    }

    @Test
    void walletAdminPage_ReturnsView() {
        WalletPageController controller = new WalletPageController();

        assertEquals("wallet/admin", controller.walletAdminPage());
    }
}
