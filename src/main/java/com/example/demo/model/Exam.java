
package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @Column(length = 30, nullable = false, unique = true)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Min(1)
    @Column(nullable = false)
    private int durationMinutes;

    @Min(1)
    @Column(nullable = false)
    private int requiredCapacity;

    @Min(1)
    @Column(nullable = false)
    private int requiredInvigilators = 1;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamStatus status = ExamStatus.ACTIVE;

    public Exam() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getRequiredCapacity() {
        return requiredCapacity;
    }

    public void setRequiredCapacity(int requiredCapacity) {
        this.requiredCapacity = requiredCapacity;
    }

    public int getRequiredInvigilators() {
        return requiredInvigilators;
    }

    public void setRequiredInvigilators(int requiredInvigilators) {
        this.requiredInvigilators = requiredInvigilators;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public void setStatus(ExamStatus status) {
        this.status = status;
    }
}
