
package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "invigilator_availability",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"invigilator_id", "time_slot_id"}
        )
)
public class InvigilatorAvailability {

    @Id
    @Column(length = 30, nullable = false, unique = true)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "invigilator_id", nullable = false)
    private Invigilator invigilator;

    @ManyToOne(optional = false)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Column(nullable = false)
    private boolean available = true;

    public InvigilatorAvailability() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Invigilator getInvigilator() {
        return invigilator;
    }

    public void setInvigilator(Invigilator invigilator) {
        this.invigilator = invigilator;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
