package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.CourseStudent;
import com.example.servermaintenance.course.domain.CourseStudentPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {
    CourseStudent findFirstByCourseAndAccount(Course course, Account account);
    List<CourseStudent> findCourseStudentsByCourseOrderByCourseLocalIndex(Course course);

    @Query("select c.courseStudentParts from CourseStudent c where c.course = :course and c.account = :account")
    List<CourseStudentPart> findStudentPartsByCourseAndAccount(Course course, Account account);
}
