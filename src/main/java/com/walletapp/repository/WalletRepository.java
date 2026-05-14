package com.walletapp.repository;

import com.walletapp.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    Optional<Wallet> findByUserId(Integer userId);

    boolean existsByUserId(Integer userId);

    @Query("SELECT w FROM Wallet w WHERE w.user.userName = :userName")
    Optional<Wallet> findWallet(@Param("userName") String userName);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w from Wallet w where w.user.userName = :userName")
    Optional<Wallet> findWalletForUpdate(@Param("userName") String userName);
}
