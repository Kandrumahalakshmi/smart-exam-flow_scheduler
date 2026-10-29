
package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedule_assignments")
public class ScheduleAssignment {

    @Id
    @Column(length = 60, nullable = false, unique = true)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    @ManyToMany
    @JoinTable(
            name = "assignment_invigilators",
            joinColumns = @JoinColumn(name = "assignment_id"),
            inverseJoinColumns = @JoinColumn(name = "invigilator_id"),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"assignment_id", "invigilator_id"}
            )
    )
    private List<Invigilator> invigilators = new ArrayList<>();
    public List<Invigilator> getInvigilators() {
        return invigilators;
    }

    public void setInvigilators(List<Invigilator> invigilators) {
        this.invigilators = invigilators;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    @Column(length = 100)
    private String reasonCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ScheduleAssignment() {
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public TimeSlot getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }


    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
