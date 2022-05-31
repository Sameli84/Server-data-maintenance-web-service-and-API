package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.CourseStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {
    CourseStudent findFirstByCourseAndAccount(Course course, Account account);
}
