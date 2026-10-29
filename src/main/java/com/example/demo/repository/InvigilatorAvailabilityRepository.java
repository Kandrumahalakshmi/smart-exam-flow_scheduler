package com.example.demo.repository;

import com.example.demo.model.InvigilatorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvigilatorAvailabilityRepository
        extends JpaRepository<InvigilatorAvailability, String> {
}