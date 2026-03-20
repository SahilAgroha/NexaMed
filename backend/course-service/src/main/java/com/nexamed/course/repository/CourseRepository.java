package com.nexamed.course.repository;

import com.nexamed.course.model.Category;
import com.nexamed.course.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    List<Course> findByPublishedTrue();

    List<Course> findByTeacherId(UUID teacherId);

    List<Course> findByCategoryAndPublishedTrue(Category category);

    @Query("SELECT c FROM Course c WHERE c.published = true AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchByKeyword(String keyword);
}