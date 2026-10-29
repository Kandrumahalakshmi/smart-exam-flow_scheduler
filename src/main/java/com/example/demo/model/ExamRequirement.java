
package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "exam_requirements")
public class ExamRequirement {

    @Id
    @Column(length = 40, nullable = false, unique = true)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String requirementType;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String requirementValue;

    @Column(length = 255)
    private String description;

    public ExamRequirement() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public String getRequirementType() {
        return requirementType;
    }

    public void setRequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

    public String getRequirementValue() {
        return requirementValue;
    }

    public void setRequirementValue(String requirementValue) {
        this.requirementValue = requirementValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
