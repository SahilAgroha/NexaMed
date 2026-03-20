package com.nexamed.course.service;

import com.nexamed.course.dto.CourseRequest;
import com.nexamed.course.dto.CourseResponse;
import com.nexamed.course.dto.EnrollmentResponse;
import com.nexamed.course.dto.ModuleRequest;
import com.nexamed.course.event.EnrollmentEvent;
import com.nexamed.course.event.EnrollmentEventPublisher;
import com.nexamed.course.model.*;
import com.nexamed.course.model.Module;
import com.nexamed.course.repository.CourseRepository;
import com.nexamed.course.repository.EnrollmentRepository;
import jakarta.persistence.EntityManager;
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
public class CourseService {

    private final CourseRepository       courseRepository;
    private final EnrollmentRepository   enrollmentRepository;
    private final EnrollmentEventPublisher eventPublisher;
    private final EntityManager          entityManager;  // used to refresh after save

    // ── Course CRUD ───────────────────────────────────────────────

    @Transactional
    public CourseResponse createCourse(CourseRequest request, String teacherId) {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty() != null ? request.getDifficulty() : Difficulty.BEGINNER)
                .category(request.getCategory())
                .thumbnailUrl(request.getThumbnailUrl())
                .teacherId(UUID.fromString(teacherId))
                .build();

        // saveAndFlush writes to DB immediately so @CreationTimestamp is populated
        Course saved = courseRepository.saveAndFlush(course);
        // refresh pulls the DB-generated values (createdAt, updatedAt) back into the entity
        entityManager.refresh(saved);
        return toResponse(saved);
    }

    public List<CourseResponse> getAllPublished() {
        return courseRepository.findByPublishedTrue()
                .stream().map(this::toResponse).toList();
    }

    public CourseResponse getCourseById(UUID id) {
        return courseRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));
    }

    public List<CourseResponse> getMyCourses(String teacherId) {
        return courseRepository.findByTeacherId(UUID.fromString(teacherId))
                .stream().map(this::toResponse).toList();
    }

    public List<CourseResponse> searchCourses(String keyword) {
        return courseRepository.searchByKeyword(keyword)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public CourseResponse publishCourse(UUID courseId, String teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        if (!course.getTeacherId().equals(UUID.fromString(teacherId))) {
            throw new RuntimeException("Only the course teacher can publish this course");
        }
        course.setPublished(true);
        Course saved = courseRepository.saveAndFlush(course);
        entityManager.refresh(saved);
        return toResponse(saved);
    }

    // ── Module management ─────────────────────────────────────────

    @Transactional
    public CourseResponse addModule(UUID courseId, ModuleRequest request, String teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        if (!course.getTeacherId().equals(UUID.fromString(teacherId))) {
            throw new RuntimeException("Only the course teacher can add modules");
        }

        Module module = Module.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .orderIndex(request.getOrderIndex())
                .course(course)
                .build();

        course.getModules().add(module);
        Course saved = courseRepository.saveAndFlush(course);
        entityManager.refresh(saved);
        return toResponse(saved);
    }

    // ── Enrollment ────────────────────────────────────────────────

    @Transactional
    public EnrollmentResponse enroll(UUID courseId, String studentId, String studentEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        if (!course.isPublished()) {
            throw new RuntimeException("Cannot enroll in an unpublished course");
        }

        UUID studentUUID = UUID.fromString(studentId);

        if (enrollmentRepository.existsByStudentIdAndCourseId(studentUUID, courseId)) {
            throw new RuntimeException("Already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .studentId(studentUUID)
                .course(course)
                .build();

        Enrollment saved = enrollmentRepository.saveAndFlush(enrollment);
        entityManager.refresh(saved);

        // Increment enrollment counter
        course.setEnrollmentCount(course.getEnrollmentCount() + 1);
        courseRepository.save(course);

        // ── Publish Kafka event ──────────────────────────────────
        EnrollmentEvent event = EnrollmentEvent.builder()
                .eventType("ENROLLMENT_CREATED")
                .studentId(studentUUID)
                .studentEmail(studentEmail)
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDifficulty(course.getDifficulty().name())
                .enrolledAt(saved.getEnrolledAt())
                .build();

        eventPublisher.publish(event);
        log.info("Student {} enrolled in '{}' — Kafka event published", studentId, course.getTitle());

        return toEnrollmentResponse(saved);
    }

    public List<EnrollmentResponse> getMyEnrollments(String studentId) {
        return enrollmentRepository.findByStudentId(UUID.fromString(studentId))
                .stream().map(this::toEnrollmentResponse).toList();
    }

    @Transactional
    public EnrollmentResponse updateProgress(UUID courseId, String studentId, int progressPercent) {
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(UUID.fromString(studentId), courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setProgressPercent(Math.min(100, Math.max(0, progressPercent)));

        if (progressPercent >= 100) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        Enrollment saved = enrollmentRepository.saveAndFlush(enrollment);
        entityManager.refresh(saved);
        return toEnrollmentResponse(saved);
    }

    // ── Mappers ───────────────────────────────────────────────────

    private CourseResponse toResponse(Course c) {
        return CourseResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .difficulty(c.getDifficulty())
                .category(c.getCategory())
                .thumbnailUrl(c.getThumbnailUrl())
                .teacherId(c.getTeacherId())
                .published(c.isPublished())
                .enrollmentCount(c.getEnrollmentCount())
                .moduleCount(c.getModules().size())
                .createdAt(c.getCreatedAt())    // now populated after refresh
                .build();
    }

    private EnrollmentResponse toEnrollmentResponse(Enrollment e) {
        return EnrollmentResponse.builder()
                .id(e.getId())
                .studentId(e.getStudentId())
                .courseId(e.getCourse().getId())
                .courseTitle(e.getCourse().getTitle())
                .status(e.getStatus())
                .progressPercent(e.getProgressPercent())
                .enrolledAt(e.getEnrolledAt())  // now populated after refresh
                .build();
    }
}