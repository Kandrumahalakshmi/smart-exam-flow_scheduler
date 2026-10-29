
package com.example.demo.repository;

import com.example.demo.model.ExamRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamRequirementRepository
        extends JpaRepository<ExamRequirement, String> {

    List<ExamRequirement> findByExam_Id(String examId);
}
