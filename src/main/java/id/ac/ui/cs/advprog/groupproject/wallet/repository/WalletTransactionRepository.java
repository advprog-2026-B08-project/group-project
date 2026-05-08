package id.ac.ui.cs.advprog.groupproject.wallet.repository;

import id.ac.ui.cs.advprog.groupproject.wallet.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId);

    List<WalletTransaction> findAllByOrderByCreatedAtDesc();
}
