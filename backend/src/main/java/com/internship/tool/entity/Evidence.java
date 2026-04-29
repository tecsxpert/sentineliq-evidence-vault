package com.internship.tool.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Required fields
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Status is required")
    private String status;

    // 🔹 New fields
    private String type;

    private String priority;

    private String caseNumber;

    private String caseName;

    private String department;

    private String assignedTo;

    private LocalDate dateCollected;

    private LocalDate deadline;

    private String location;

    private String source;

    @Column(length = 1000)
    private String description;

    private String tags;
}