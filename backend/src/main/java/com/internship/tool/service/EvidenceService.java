package com.internship.tool.service;

import com.internship.tool.entity.Evidence;
import com.internship.tool.entity.User;
import com.internship.tool.repository.EvidenceRepository;
import com.internship.tool.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class EvidenceService {

    private final EvidenceRepository repository;

    @Autowired
    private UserRepository userRepository;

    public EvidenceService(EvidenceRepository repository) {
        this.repository = repository;
    }

    // ✅ USER-SPECIFIC LIST
    public Page<Evidence> getAllEvidence(Pageable pageable) {
        String username = getCurrentUsername();
        return repository.findByUserUsername(username, pageable);
    }

    // ✅ USER-SPECIFIC SEARCH
    public Page<Evidence> searchEvidence(String keyword, Pageable pageable) {
        String username = getCurrentUsername();
        return repository.findByUserUsernameAndNameContainingIgnoreCase(username, keyword, pageable);
    }

    // ✅ CREATE WITH USER
    public Evidence createEvidence(Evidence evidence) {
        String username = getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        evidence.setUser(user);

        return repository.save(evidence);
    }

    // ✅ SECURE UPDATE (only owner can update)
    public Evidence updateEvidence(Long id, Evidence newData) {
        String username = getCurrentUsername();

        return repository.findById(id).map(evidence -> {

            if (!evidence.getUser().getUsername().equals(username)) {
                throw new RuntimeException("Unauthorized");
            }

            evidence.setName(newData.getName());
            evidence.setStatus(newData.getStatus());
            evidence.setType(newData.getType());
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

    // ✅ SECURE DELETE
    public void deleteEvidence(Long id) {
        String username = getCurrentUsername();

        Evidence evidence = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evidence not found"));

        if (!evidence.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        repository.delete(evidence);
    }

    // ✅ USER-SPECIFIC DASHBOARD
    public Map<String, Long> getDashboardData() {
        String username = getCurrentUsername();

        Map<String, Long> data = new HashMap<>();

        data.put("total", repository.countByUserUsername(username));
        data.put("active", repository.countByUserUsernameAndStatusIgnoreCase(username, "active"));
        data.put("pending", repository.countByUserUsernameAndStatusIgnoreCase(username, "pending"));
        data.put("completed", repository.countByUserUsernameAndStatusIgnoreCase(username, "completed"));

        return data;
    }

    // ✅ GET USERNAME FROM JWT
    public String getCurrentUsername() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
    public byte[] exportEvidenceToCSV() {
        String username = getCurrentUsername();

        List<Evidence> list = repository.findByUserUsername(username, Pageable.unpaged()).getContent();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        // CSV Header
        writer.println("ID,Name,Type,Status,Priority,AssignedTo,DateCollected,Deadline");

        // Data
        for (Evidence e : list) {
            writer.println(
                    e.getId() + "," +
                            e.getName() + "," +
                            e.getType() + "," +
                            e.getStatus() + "," +
                            e.getPriority() + "," +
                            e.getAssignedTo() + "," +
                            e.getDateCollected() + "," +
                            e.getDeadline()
            );
        }

        writer.flush();
        return out.toByteArray();
    }
}