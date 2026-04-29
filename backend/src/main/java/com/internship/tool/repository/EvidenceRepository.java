package com.internship.tool.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.internship.tool.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    Page<Evidence> findAll(Pageable pageable);

    Page<Evidence> findByNameContainingIgnoreCaseOrTypeContainingIgnoreCaseOrStatusContainingIgnoreCase(
            String name,
            String type,
            String status,
            Pageable pageable
    );
    long countByStatusIgnoreCase(String status);
}