package com.nexamed.analytics.repository;

import com.nexamed.analytics.model.StudentAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentAnalyticsRepository extends JpaRepository<StudentAnalytics, UUID> {
    Optional<StudentAnalytics> findByStudentId(UUID studentId);
}