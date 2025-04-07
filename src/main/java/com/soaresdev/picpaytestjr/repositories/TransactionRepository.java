package com.soaresdev.picpaytestjr.repositories;

import com.soaresdev.picpaytestjr.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}