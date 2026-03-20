package com.nexamed.course.repository;

import com.nexamed.course.model.Enrollment;
import com.nexamed.course.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findByStudentId(UUID studentId);

    List<Enrollment> findByCourseId(UUID courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);

    long countByCourseIdAndStatus(UUID courseId, EnrollmentStatus status);
}