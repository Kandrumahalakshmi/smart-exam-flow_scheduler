
package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "accommodations")
public class Accommodation {

    @Id
    @Column(length = 30, nullable = false, unique = true)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccommodationType type;

    @Min(0)
    @Column(nullable = false)
    private int extraTimeMinutes = 0;

    @Column(length = 255)
    private String details;

    @Column(nullable = false)
    private boolean active = true;

    public Accommodation() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public AccommodationType getType() {
        return type;
    }

    public void setType(AccommodationType type) {
        this.type = type;
    }

    public int getExtraTimeMinutes() {
        return extraTimeMinutes;
    }

    public void setExtraTimeMinutes(int extraTimeMinutes) {
        this.extraTimeMinutes = extraTimeMinutes;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
