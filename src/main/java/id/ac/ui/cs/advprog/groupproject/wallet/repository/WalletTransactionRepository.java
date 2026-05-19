package id.ac.ui.cs.advprog.groupproject.wallet.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import id.ac.ui.cs.advprog.groupproject.wallet.model.WalletTransaction;
import jakarta.persistence.LockModeType;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId);

    List<WalletTransaction> findAllByOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from WalletTransaction t where t.id = :id")
    java.util.Optional<WalletTransaction> findByIdForUpdate(@Param("id") UUID id);
}
