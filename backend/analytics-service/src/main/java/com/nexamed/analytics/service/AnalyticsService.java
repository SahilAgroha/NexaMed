package com.nexamed.analytics.service;

import com.nexamed.analytics.dto.EnrollmentEvent;
import com.nexamed.analytics.dto.InterviewCompletedEvent;
import com.nexamed.analytics.dto.StudentDashboardResponse;
import com.nexamed.analytics.model.ActivityRecord;
import com.nexamed.analytics.model.ActivityType;
import com.nexamed.analytics.model.StudentAnalytics;
import com.nexamed.analytics.repository.ActivityRecordRepository;
import com.nexamed.analytics.repository.StudentAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final StudentAnalyticsRepository analyticsRepo;
    private final ActivityRecordRepository   activityRepo;

    // ── Event handlers (called by Kafka consumers) ─────────────────

    @Transactional
    public void handleEnrollment(EnrollmentEvent event) {
        StudentAnalytics analytics = getOrCreate(event.getStudentId());

        analytics.setTotalEnrollments(analytics.getTotalEnrollments() + 1);
        analytics.setLastActiveAt(LocalDateTime.now());

        analyticsRepo.save(analytics);

        // Log individual activity record
        activityRepo.save(ActivityRecord.builder()
                .studentId(event.getStudentId())
                .activityType(ActivityType.ENROLLMENT)
                .referenceId(event.getCourseId() != null ? event.getCourseId().toString() : null)
                .referenceName(event.getCourseTitle())
                .build());

        log.info("Analytics updated — ENROLLMENT: student={}, course='{}'",
                event.getStudentId(), event.getCourseTitle());
    }

    @Transactional
    public void handleInterviewCompleted(InterviewCompletedEvent event) {
        StudentAnalytics analytics = getOrCreate(event.getStudentId());

        int total    = analytics.getTotalInterviews() + 1;
        int newScore = event.getOverallScore() != null ? event.getOverallScore() : 0;

        // Recalculate running average
        int currentAvg = analytics.getAverageInterviewScore();
        int newAvg = ((currentAvg * analytics.getTotalInterviews()) + newScore) / total;

        analytics.setTotalInterviews(total);
        analytics.setAverageInterviewScore(newAvg);
        analytics.setBestInterviewScore(Math.max(analytics.getBestInterviewScore(), newScore));
        analytics.setLastActiveAt(LocalDateTime.now());

        analyticsRepo.save(analytics);

        activityRepo.save(ActivityRecord.builder()
                .studentId(event.getStudentId())
                .activityType(ActivityType.INTERVIEW_COMPLETED)
                .referenceId(event.getSessionId() != null ? event.getSessionId().toString() : null)
                .referenceName(event.getSpecialty())
                .score(newScore)
                .build());

        log.info("Analytics updated — INTERVIEW: student={}, score={}, avg={}",
                event.getStudentId(), newScore, newAvg);
    }

    // ── Query methods (called by REST controller) ──────────────────

    public StudentDashboardResponse getDashboard(String studentId) {
        UUID uuid = UUID.fromString(studentId);
        StudentAnalytics analytics = getOrCreate(uuid);

        List<ActivityRecord> recent = activityRepo
                .findRecentActivity(uuid, LocalDateTime.now().minusDays(30));

        List<StudentDashboardResponse.ActivitySummary> summaries = recent.stream()
                .map(a -> StudentDashboardResponse.ActivitySummary.builder()
                        .type(a.getActivityType().name())
                        .description(buildDescription(a))
                        .score(a.getScore())
                        .occurredAt(a.getOccurredAt())
                        .build())
                .toList();

        return StudentDashboardResponse.builder()
                .studentId(uuid)
                .totalEnrollments(analytics.getTotalEnrollments())
                .completedCourses(analytics.getCompletedCourses())
                .totalInterviews(analytics.getTotalInterviews())
                .averageInterviewScore(analytics.getAverageInterviewScore())
                .bestInterviewScore(analytics.getBestInterviewScore())
                .totalQuizzesTaken(analytics.getTotalQuizzesTaken())
                .averageQuizScore(analytics.getAverageQuizScore())
                .streakDays(analytics.getStreakDays())
                .recentActivity(summaries)
                .lastActiveAt(analytics.getLastActiveAt())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    public List<ActivityRecord> getActivityTimeline(String studentId) {
        return activityRepo.findByStudentIdOrderByOccurredAtDesc(UUID.fromString(studentId));
    }

    // ── Helpers ────────────────────────────────────────────────────

    private StudentAnalytics getOrCreate(UUID studentId) {
        return analyticsRepo.findByStudentId(studentId)
                .orElseGet(() -> analyticsRepo.save(
                        StudentAnalytics.builder().studentId(studentId).build()));
    }

    private String buildDescription(ActivityRecord a) {
        return switch (a.getActivityType()) {
            case ENROLLMENT         -> "Enrolled in: " + a.getReferenceName();
            case INTERVIEW_COMPLETED-> "Completed interview: " + a.getReferenceName()
                    + (a.getScore() != null ? " (Score: " + a.getScore() + ")" : "");
            case QUIZ_SUBMITTED     -> "Submitted quiz: " + a.getReferenceName();
            case COURSE_COMPLETED   -> "Completed course: " + a.getReferenceName();
        };
    }
}