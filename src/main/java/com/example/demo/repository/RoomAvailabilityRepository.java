
package com.example.demo.repository;

import com.example.demo.model.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomAvailabilityRepository
        extends JpaRepository<RoomAvailability, String> {
}
