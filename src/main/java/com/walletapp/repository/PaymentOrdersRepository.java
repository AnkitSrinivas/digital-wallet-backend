package com.walletapp.repository;

import com.walletapp.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentOrdersRepository extends JpaRepository<PaymentOrder, Integer> {

    Optional<PaymentOrder> findByOrderId(String orderId);
}
