package com.internship.tool.config;

import com.internship.tool.entity.Evidence;
import com.internship.tool.repository.EvidenceRepository;
import com.internship.tool.service.EmailNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReminderScheduler {

    private final EvidenceRepository evidenceRepository;
    private final EmailNotificationService emailNotificationService;

    public ReminderScheduler(EvidenceRepository evidenceRepository,
                             EmailNotificationService emailNotificationService) {
        this.evidenceRepository = evidenceRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendDailyDeadlineReminders() {
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(2);

        List<Evidence> dueSoon = new ArrayList<>();
        dueSoon.addAll(evidenceRepository.findByDeadlineBetweenAndStatusIgnoreCase(today, soon, "ACTIVE"));
        dueSoon.addAll(evidenceRepository.findByDeadlineBetweenAndStatusIgnoreCase(today, soon, "PENDING"));

        for (Evidence evidence : dueSoon) {
            emailNotificationService.sendDeadlineReminder(evidence);
        }
    }
}
