package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {
    CourseStudent findFirstByCourseAndAccount(Course course, Account account);
}
