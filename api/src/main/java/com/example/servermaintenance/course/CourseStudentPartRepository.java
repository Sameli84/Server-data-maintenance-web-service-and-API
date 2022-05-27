package com.example.servermaintenance.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseStudentPartRepository extends JpaRepository<CourseStudentPart, Long> {
    // don't ask!
    List<CourseStudentPart> findCourseStudentPartsByCourseStudentOrderBySchemaPart_Order(CourseStudent courseStudent);
}
