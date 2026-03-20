package com.nexamed.analytics.repository;

import com.nexamed.analytics.model.ActivityRecord;
import com.nexamed.analytics.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, UUID> {

    List<ActivityRecord> findByStudentIdOrderByOccurredAtDesc(UUID studentId);

    List<ActivityRecord> findByStudentIdAndActivityType(UUID studentId, ActivityType type);

    @Query("SELECT a FROM ActivityRecord a WHERE a.studentId = :studentId " +
            "AND a.occurredAt >= :from ORDER BY a.occurredAt DESC")
    List<ActivityRecord> findRecentActivity(UUID studentId, LocalDateTime from);

    @Query("SELECT a.referenceId, COUNT(a) as count FROM ActivityRecord a " +
            "WHERE a.activityType = 'ENROLLMENT' GROUP BY a.referenceId " +
            "ORDER BY count DESC")
    List<Object[]> findMostPopularCourses();
}