
package com.example.demo.repository;

import com.example.demo.model.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccommodationRepository
        extends JpaRepository<Accommodation, String> {

    List<Accommodation> findByStudent_IdAndActiveTrue(
            String studentId
    );
}
