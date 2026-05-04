package com.internship.tool.service;

import com.internship.tool.entity.Evidence;
import com.internship.tool.entity.User;
import com.internship.tool.repository.EvidenceRepository;
import com.internship.tool.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class EvidenceService {

    private static final Set<String> ALLOWED_UPLOAD_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "text/plain",
            "text/csv"
    );

    private final EvidenceRepository repository;
    private final UserRepository userRepository;
    private final Path uploadDirectory;
    private final long maxUploadSizeBytes;

    public EvidenceService(EvidenceRepository repository,
                           UserRepository userRepository,
                           @Value("${app.upload.dir:uploads}") String uploadDirectory,
                           @Value("${app.upload.max-size-bytes:5242880}") long maxUploadSizeBytes) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        this.maxUploadSizeBytes = maxUploadSizeBytes;
    }

    public Page<Evidence> getAllEvidence(Pageable pageable) {
        return repository.findByUserUsername(getCurrentUsername(), pageable);
    }

    public Page<Evidence> searchEvidence(String keyword, String type, String status,
                                         LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return repository.searchForUser(getCurrentUsername(), keyword, type, status, fromDate, toDate, pageable);
    }

    public Evidence createEvidence(Evidence evidence) {
        evidence.setUser(currentUser());
        return repository.save(evidence);
    }

    public Evidence updateEvidence(Long id, Evidence newData) {
        Evidence evidence = findOwnedEvidence(id);

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
    }

    public void deleteEvidence(Long id) {
        repository.delete(findOwnedEvidence(id));
    }

    public Map<String, Long> getDashboardData() {
        String username = getCurrentUsername();

        Map<String, Long> data = new LinkedHashMap<>();
        data.put("total", repository.countByUserUsername(username));
        data.put("active", repository.countByUserUsernameAndStatusIgnoreCase(username, "active"));
        data.put("pending", repository.countByUserUsernameAndStatusIgnoreCase(username, "pending"));
        data.put("completed", repository.countByUserUsernameAndStatusIgnoreCase(username, "completed"));
        return data;
    }

    public Map<String, Object> getAnalyticsData() {
        String username = getCurrentUsername();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", rowsToMap(repository.countByStatusForUser(username)));
        data.put("type", rowsToMap(repository.countByTypeForUser(username)));
        data.put("priority", rowsToMap(repository.countByPriorityForUser(username)));
        data.put("dashboard", getDashboardData());
        return data;
    }

    public byte[] exportEvidenceToCSV() {
        List<Evidence> list = repository.findByUserUsername(getCurrentUsername(), Pageable.unpaged()).getContent();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("ID,Name,Type,Status,Priority,AssignedTo,DateCollected,Deadline,CaseNumber,Department");

        for (Evidence e : list) {
            writer.println(String.join(",",
                    csv(e.getId()),
                    csv(e.getName()),
                    csv(e.getType()),
                    csv(e.getStatus()),
                    csv(e.getPriority()),
                    csv(e.getAssignedTo()),
                    csv(e.getDateCollected()),
                    csv(e.getDeadline()),
                    csv(e.getCaseNumber()),
                    csv(e.getDepartment())
            ));
        }

        writer.flush();
        return out.toByteArray();
    }

    public Map<String, Object> uploadFile(MultipartFile file, Evidence uploadData) {
        validateFile(file);

        try {
            Path uploadDir = uploadDirectory;
            Files.createDirectories(uploadDir);

            String originalName = file.getOriginalFilename() == null ? "evidence-file" : file.getOriginalFilename();
            String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storedName = UUID.randomUUID() + "_" + safeName;
            Path destination = uploadDir.resolve(storedName).normalize();

            if (!destination.startsWith(uploadDir)) {
                throw new IllegalArgumentException("Invalid file path");
            }

            file.transferTo(destination);

            Evidence evidence = new Evidence();
            evidence.setName(firstText(uploadData.getName(), originalName));
            evidence.setType(firstText(uploadData.getType(), "Document"));
            evidence.setStatus(firstText(uploadData.getStatus(), "PENDING"));
            evidence.setPriority(uploadData.getPriority());
            evidence.setCaseNumber(uploadData.getCaseNumber());
            evidence.setCaseName(uploadData.getCaseName());
            evidence.setDepartment(uploadData.getDepartment());
            evidence.setAssignedTo(uploadData.getAssignedTo());
            evidence.setDateCollected(uploadData.getDateCollected() == null ? LocalDate.now() : uploadData.getDateCollected());
            evidence.setDeadline(uploadData.getDeadline());
            evidence.setLocation(uploadData.getLocation());
            evidence.setSource(firstText(uploadData.getSource(), storedName));
            evidence.setDescription(firstText(uploadData.getDescription(), "Uploaded file: " + originalName));
            evidence.setTags(uploadData.getTags());
            Evidence saved = createEvidence(evidence);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", saved.getId());
            result.put("fileName", storedName);
            result.put("size", file.getSize());
            return result;
        } catch (IOException ex) {
            throw new IllegalArgumentException("File upload failed");
        }
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Authentication required");
        }
        return authentication.getName();
    }

    private Evidence findOwnedEvidence(Long id) {
        Evidence evidence = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evidence not found"));

        if (evidence.getUser() == null || !evidence.getUser().getUsername().equals(getCurrentUsername())) {
            throw new AccessDeniedException("You cannot access this evidence");
        }

        return evidence;
    }

    private User currentUser() {
        return userRepository.findByUsername(getCurrentUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.getSize() > maxUploadSizeBytes) {
            throw new IllegalArgumentException("File exceeds the configured upload size limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_UPLOAD_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type");
        }
    }

    private String firstText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private Map<String, Long> rowsToMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = row[0] == null ? "Unspecified" : row[0].toString();
            map.put(key, (Long) row[1]);
        }
        return map;
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().replace("\"", "\"\"");
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text + "\"";
        }
        return text;
    }
}
