package com.internship.tool.config;

import com.internship.tool.entity.Evidence;
import com.internship.tool.entity.User;
import com.internship.tool.repository.EvidenceRepository;
import com.internship.tool.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EvidenceRepository evidenceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, EvidenceRepository evidenceRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.evidenceRepository = evidenceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0 || evidenceRepository.count() > 0) {
            return;
        }

        User analyst = new User(null, "analyst", "analyst@example.com", "9876543210", passwordEncoder.encode("password"));
        User investigator = new User(null, "investigator", "investigator@example.com", "9876543211", passwordEncoder.encode("password"));
        userRepository.saveAll(List.of(analyst, investigator));

        evidenceRepository.saveAll(List.of(
                item("Laptop Image", "Digital", "ACTIVE", "HIGH", "CY-2026-101", "Phishing Ring", "Cyber Crime", "Asha Rao", -12, 2, "Lab A", "forensic-image-101.dd", "Disk image from suspect laptop", "laptop,hash", analyst),
                item("Email Header Set", "Digital", "PENDING", "MEDIUM", "CY-2026-101", "Phishing Ring", "Cyber Crime", "Nikhil Sen", -8, 4, "Secure Inbox", "headers.eml", "Headers from reported phishing messages", "email,headers", analyst),
                item("USB Drive", "Physical", "ACTIVE", "HIGH", "CY-2026-104", "Data Exfiltration", "Forensics", "Meera Iyer", -5, 1, "Evidence Locker 3", "Seized device", "Encrypted USB storage device", "usb,storage", analyst),
                item("CCTV Clip", "Digital", "COMPLETED", "LOW", "CR-2026-088", "Warehouse Access", "Police", "Arjun Das", -20, -1, "Warehouse 7", "camera-2.mp4", "Entry camera clip", "video,cctv", analyst),
                item("Access Log Export", "Document", "PENDING", "MEDIUM", "CY-2026-108", "Credential Abuse", "Cyber Crime", "Leena Shah", -3, 7, "SIEM", "access-log.csv", "Access log export for review", "logs,access", analyst),
                item("Mobile Screenshot", "Digital", "ACTIVE", "LOW", "CR-2026-091", "Harassment Report", "Police", "Ravi Menon", -2, 5, "Case Folder", "screenshot.png", "Chat screenshot submitted by victim", "mobile,chat", analyst),
                item("Network Capture", "Digital", "ACTIVE", "HIGH", "CY-2026-112", "Malware Beacon", "Cyber Crime", "Asha Rao", -1, 3, "SOC", "capture.pcap", "Packet capture with suspicious beaconing", "pcap,malware", analyst),
                item("Signed Statement", "Document", "COMPLETED", "MEDIUM", "CR-2026-094", "Witness Statement", "Police", "Kiran Patel", -16, -4, "Records Room", "statement.pdf", "Signed witness statement", "witness,statement", analyst),
                item("Server Snapshot", "Digital", "PENDING", "HIGH", "CY-2026-117", "Ransomware Triage", "Forensics", "Meera Iyer", -4, 2, "Cloud Vault", "snapshot-117", "Snapshot of impacted server", "server,ransomware", investigator),
                item("Door Badge Report", "Document", "ACTIVE", "MEDIUM", "CR-2026-099", "Unauthorized Entry", "Police", "Arjun Das", -7, 6, "Security Office", "badge-report.xlsx", "Badge activity report", "badge,entry", investigator),
                item("Recovered Invoice", "Document", "COMPLETED", "LOW", "FR-2026-021", "Invoice Fraud", "Forensics", "Leena Shah", -22, -2, "Archive", "invoice.pdf", "Recovered fraudulent invoice", "invoice,fraud", investigator),
                item("Memory Dump", "Digital", "ACTIVE", "HIGH", "CY-2026-119", "Endpoint Malware", "Cyber Crime", "Nikhil Sen", -6, 1, "Lab B", "memdump.raw", "Memory dump from compromised endpoint", "memory,malware", investigator),
                item("Fingerprint Card", "Physical", "PENDING", "MEDIUM", "CR-2026-105", "Vehicle Break-in", "Forensics", "Kiran Patel", -9, 8, "Evidence Locker 1", "card-105", "Fingerprint lift card", "fingerprint,physical", investigator),
                item("Call Detail Record", "Document", "ACTIVE", "MEDIUM", "CR-2026-107", "Fraud Calls", "Police", "Ravi Menon", -11, 9, "Telecom Portal", "cdr.csv", "Call detail records from provider", "cdr,telecom", investigator),
                item("Hash Verification Sheet", "Document", "COMPLETED", "LOW", "CY-2026-101", "Phishing Ring", "Forensics", "Meera Iyer", -10, -3, "Lab A", "hashes.txt", "SHA-256 verification sheet", "hash,chain-of-custody", investigator)
        ));
    }

    private Evidence item(String name, String type, String status, String priority, String caseNumber,
                          String caseName, String department, String assignedTo, int collectedOffset,
                          int deadlineOffset, String location, String source, String description,
                          String tags, User user) {
        Evidence evidence = new Evidence();
        evidence.setName(name);
        evidence.setType(type);
        evidence.setStatus(status);
        evidence.setPriority(priority);
        evidence.setCaseNumber(caseNumber);
        evidence.setCaseName(caseName);
        evidence.setDepartment(department);
        evidence.setAssignedTo(assignedTo);
        evidence.setDateCollected(LocalDate.now().plusDays(collectedOffset));
        evidence.setDeadline(LocalDate.now().plusDays(deadlineOffset));
        evidence.setLocation(location);
        evidence.setSource(source);
        evidence.setDescription(description);
        evidence.setTags(tags);
        evidence.setUser(user);
        return evidence;
    }
}
