
package com.example.demo.repository;

import com.example.demo.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository
        extends JpaRepository<TimeSlot, String> {
}
