package com.walletapp.repository;

import com.walletapp.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    Page<Transaction> findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc(Integer senderWalletId, Integer receiverWalletId, Pageable pageable);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
