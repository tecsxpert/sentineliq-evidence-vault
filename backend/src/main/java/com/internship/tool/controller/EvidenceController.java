package com.internship.tool.controller;

import com.internship.tool.entity.Evidence;
import com.internship.tool.service.EvidenceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@CrossOrigin
public class EvidenceController {

    private final EvidenceService service;

    public EvidenceController(EvidenceService service) {
        this.service = service;
    }

    @Operation(summary = "Search evidence for the current user with optional filters")
    @GetMapping("/search")
    public Page<Evidence> searchEvidence(@RequestParam(required = false, defaultValue = "") String q,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                         Pageable pageable) {
        return service.searchEvidence(q, type, status, fromDate, toDate, pageable);
    }

    @Operation(summary = "List evidence for the current user")
    @GetMapping("/all")
    public Page<Evidence> getAllEvidence(Pageable pageable) {
        return service.getAllEvidence(pageable);
    }

    @Operation(summary = "Create evidence for the current user")
    @PostMapping("/create")
    public Evidence create(@Valid @RequestBody Evidence evidence) {
        return service.createEvidence(evidence);
    }

    @Operation(summary = "Update evidence owned by the current user")
    @PutMapping("/{id}")
    public Evidence updateEvidence(@PathVariable Long id, @Valid @RequestBody Evidence evidence) {
        return service.updateEvidence(id, evidence);
    }

    @Operation(summary = "Delete evidence owned by the current user")
    @DeleteMapping("/{id}")
    public void deleteEvidence(@PathVariable Long id) {
        service.deleteEvidence(id);
    }

    @Operation(summary = "Dashboard KPI data for the current user")
    @GetMapping({"/dashboard", "/stats"})
    public Map<String, Long> getDashboard() {
        return service.getDashboardData();
    }

    @Operation(summary = "Analytics chart data for the current user")
    @GetMapping("/analytics")
    public Map<String, Object> getAnalytics() {
        return service.getAnalyticsData();
    }

    @Operation(summary = "Export current user's evidence as CSV")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCSV() {
        byte[] data = service.exportEvidenceToCSV();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=evidence.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(data);
    }

    @Operation(summary = "Upload a validated evidence file")
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam MultipartFile file,
                                      @RequestParam(required = false) String name,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String priority,
                                      @RequestParam(required = false) String caseNumber,
                                      @RequestParam(required = false) String caseName,
                                      @RequestParam(required = false) String department,
                                      @RequestParam(required = false) String assignedTo,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateCollected,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
                                      @RequestParam(required = false) String location,
                                      @RequestParam(required = false) String source,
                                      @RequestParam(required = false) String description,
                                      @RequestParam(required = false) String tags) {
        Evidence evidence = new Evidence();
        evidence.setName(name);
        evidence.setType(type);
        evidence.setStatus(status);
        evidence.setPriority(priority);
        evidence.setCaseNumber(caseNumber);
        evidence.setCaseName(caseName);
        evidence.setDepartment(department);
        evidence.setAssignedTo(assignedTo);
        evidence.setDateCollected(dateCollected);
        evidence.setDeadline(deadline);
        evidence.setLocation(location);
        evidence.setSource(source);
        evidence.setDescription(description);
        evidence.setTags(tags);
        return service.uploadFile(file, evidence);
    }
}
