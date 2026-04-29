package com.internship.tool.controller;

import com.internship.tool.entity.Evidence;
import com.internship.tool.service.EvidenceService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@CrossOrigin
public class EvidenceController {

    private final EvidenceService service;

    public EvidenceController(EvidenceService service) {
        this.service = service;
    }
   /* @GetMapping("/search")
    public Page<Evidence> searchEvidence(@RequestParam String q, Pageable pageable) {
        return service.search(q, pageable,);
    }*/

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
    @GetMapping("/dashboard")
    public Map<String, Long> getDashboard() {
        Map<String, Long> data = new HashMap<>();

        data.put("total", service.getTotal());
        data.put("active", service.getByStatus("ACTIVE"));
        data.put("pending", service.getByStatus("PENDING"));
        data.put("completed", service.getByStatus("COMPLETED"));

        return data;
    }
}