package com.internship.tool.service;

import com.internship.tool.entity.Evidence;
import com.internship.tool.repository.EvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
public class EvidenceService {

    private final EvidenceRepository repository;

    public EvidenceService(EvidenceRepository repository) {
        this.repository = repository;
    }

    // GET ALL
    public Page<Evidence> getAllEvidence(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Evidence> searchEvidence(String keyword, Pageable pageable) {
        return repository
                .findByNameContainingIgnoreCaseOrTypeContainingIgnoreCaseOrStatusContainingIgnoreCase(
                        keyword, keyword, keyword, pageable
                );
    }
    // CREATE
    public Evidence createEvidence(Evidence evidence) {
        return repository.save(evidence);
    }

    // UPDATE
    public Evidence updateEvidence(Long id, Evidence newData) {
        return repository.findById(id).map(evidence -> {
            evidence.setName(newData.getName());
            evidence.setStatus(newData.getStatus());
            evidence.setName(newData.getName());
            evidence.setType(newData.getType());
            evidence.setStatus(newData.getStatus());
            evidence.setPriority(newData.getPriority());
            evidence.setCaseNumber(newData.getCaseNumber());
            evidence.setCaseName(newData.getCaseName());
            evidence.setDepartment(newData.getDepartment());
            evidence.setAssignedTo(newData.getAssignedTo());
            evidence.setDateCollected(newData.getDateCollected());
            evidence.setDeadline(newData.getDeadline());
            evidence.setLocation(newData.getLocation());
            evidence.setSource(newData.getSource());
            evidence.setDescription(newData.getDescription());
            evidence.setTags(newData.getTags());
            return repository.save(evidence);
        }).orElseThrow(() -> new RuntimeException("Evidence not found"));
    }

    // DELETE
    public void deleteEvidence(Long id) {
        repository.deleteById(id);
    }
    public long getTotal() {
        return repository.count();
    }

    public long getByStatus(String status) {
        return repository.countByStatusIgnoreCase(status);
    }
}