package com.example.servermaintenance.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseDataPartRepository extends JpaRepository<CourseDataPart, Long> {
    // don't ask!
    List<CourseDataPart> findCourseDataPartsByCourseStudentDataOrderByCourseSchemaPart_Order(CourseStudentData courseStudentData);
}
