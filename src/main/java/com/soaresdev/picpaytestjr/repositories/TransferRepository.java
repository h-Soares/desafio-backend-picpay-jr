package com.soaresdev.picpaytestjr.repositories;

import com.soaresdev.picpaytestjr.entities.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
}