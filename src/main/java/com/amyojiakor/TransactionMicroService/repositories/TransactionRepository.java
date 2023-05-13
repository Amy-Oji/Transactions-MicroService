package com.amyojiakor.TransactionMicroService.repositories;

import com.amyojiakor.TransactionMicroService.models.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository <Transaction, Long> {
    Optional<Transaction> findByReferenceNumber(String refNum);

}
