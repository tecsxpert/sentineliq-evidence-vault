package com.internship.tool.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.internship.tool.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    Page<Evidence> findByUserUsername(String username, Pageable pageable);

    Page<Evidence> findByUserUsernameAndNameContainingIgnoreCase(
            String username,
            String name,
            Pageable pageable
    );

    // ✅ DASHBOARD FIX
    long countByUserUsername(String username);

    long countByUserUsernameAndStatusIgnoreCase(String username, String status);
}