package com.learning;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<LedgerEntry> findAllByOrderByCreatedAtDesc();
}
