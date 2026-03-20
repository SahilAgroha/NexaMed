package com.nexamed.course.controller;

import com.nexamed.course.dto.*;
import com.nexamed.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // ── Course CRUD ───────────────────────────────────────────────

    /** GET /api/courses — all published courses (public) */
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllPublished());
    }

    /** GET /api/courses/{id} — single course */
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    /** GET /api/courses/search?keyword=pharmacology */
    @GetMapping("/search")
    public ResponseEntity<List<CourseResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }

    /** GET /api/courses/my — teacher's own courses */
    @GetMapping("/my")
    public ResponseEntity<List<CourseResponse>> getMyCourses(
            @RequestHeader("X-User-Id") String teacherId) {
        return ResponseEntity.ok(courseService.getMyCourses(teacherId));
    }

    /** POST /api/courses — create course (TEACHER only) */
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseRequest request,
            @RequestHeader("X-User-Id")   String teacherId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("TEACHER") && !role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.createCourse(request, teacherId));
    }

    /** PUT /api/courses/{id}/publish — publish course */
    @PutMapping("/{id}/publish")
    public ResponseEntity<CourseResponse> publishCourse(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String teacherId) {
        return ResponseEntity.ok(courseService.publishCourse(id, teacherId));
    }

    /** POST /api/courses/{id}/modules — add module to course */
    @PostMapping("/{id}/modules")
    public ResponseEntity<CourseResponse> addModule(
            @PathVariable UUID id,
            @Valid @RequestBody ModuleRequest request,
            @RequestHeader("X-User-Id") String teacherId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.addModule(id, request, teacherId));
    }

    // ── Enrollment ────────────────────────────────────────────────

    /** POST /api/courses/{id}/enroll — student enrolls */
    @PostMapping("/{id}/enroll")
    public ResponseEntity<EnrollmentResponse> enroll(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id")    String studentId,
            @RequestHeader("X-User-Email") String studentEmail) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.enroll(id, studentId, studentEmail));
    }

    /** GET /api/courses/enrollments/my — student's enrolled courses */
    @GetMapping("/enrollments/my")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.ok(courseService.getMyEnrollments(studentId));
    }

    /** PUT /api/courses/{id}/progress?percent=50 */
    @PutMapping("/{id}/progress")
    public ResponseEntity<EnrollmentResponse> updateProgress(
            @PathVariable UUID id,
            @RequestParam int percent,
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.ok(courseService.updateProgress(id, studentId, percent));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Course service is running");
    }
}