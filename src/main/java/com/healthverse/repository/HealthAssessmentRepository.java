package com.healthverse.repository;

import com.healthverse.model.HealthAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthAssessmentRepository extends JpaRepository<HealthAssessment, String> {
}
