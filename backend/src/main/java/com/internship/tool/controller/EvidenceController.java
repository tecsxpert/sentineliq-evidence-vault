package com.internship.tool.controller;

import com.internship.tool.entity.Evidence;
import com.internship.tool.service.EvidenceService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;


@RestController
@CrossOrigin
public class EvidenceController {

    private final EvidenceService service;

    public EvidenceController(EvidenceService service) {
        this.service = service;
    }

    // ✅ FIXED SEARCH
    @GetMapping("/search")
    public Page<Evidence> searchEvidence(@RequestParam String q, Pageable pageable) {
        return service.searchEvidence(q, pageable);
    }

    @GetMapping("/all")
    public Page<Evidence> getAllEvidence(Pageable pageable) {
        return service.getAllEvidence(pageable);
    }

    @PostMapping("/create")
    public Evidence create(@RequestBody Evidence evidence) {
        return service.createEvidence(evidence);
    }

    @PutMapping("/{id}")
    public Evidence updateEvidence(@PathVariable Long id, @Valid @RequestBody Evidence evidence) {
        return service.updateEvidence(id, evidence);
    }

    @DeleteMapping("/{id}")
    public void deleteEvidence(@PathVariable Long id) {
        service.deleteEvidence(id);
    }

    // ✅ FIXED DASHBOARD (user-specific)
    @GetMapping("/dashboard")
    public Map<String, Long> getDashboard() {
        return service.getDashboardData();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCSV() {

        byte[] data = service.exportEvidenceToCSV();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=evidence.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(data);
    }
}