package com.internship.tool.config;

import com.internship.tool.entity.AuditLog;
import com.internship.tool.entity.Evidence;
import com.internship.tool.repository.AuditLogRepository;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditLoggingAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditLoggingAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @AfterReturning(pointcut = "execution(* com.internship.tool.service.EvidenceService.createEvidence(..))", returning = "result")
    public void logCreate(Object result) {
        if (result instanceof Evidence evidence) {
            save("CREATE", evidence.getId(), "Created evidence " + evidence.getName());
        }
    }

    @AfterReturning(pointcut = "execution(* com.internship.tool.service.EvidenceService.updateEvidence(..))", returning = "result")
    public void logUpdate(Object result) {
        if (result instanceof Evidence evidence) {
            save("UPDATE", evidence.getId(), "Updated evidence " + evidence.getName());
        }
    }

    @AfterReturning("execution(* com.internship.tool.service.EvidenceService.deleteEvidence(..)) && args(id)")
    public void logDelete(Long id) {
        save("DELETE", id, "Deleted evidence " + id);
    }

    private void save(String action, Long entityId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityName("Evidence");
        log.setEntityId(entityId);
        log.setUsername(currentUsername());
        log.setCreatedAt(LocalDateTime.now());
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "system";
        }
        return authentication.getName();
    }
}
