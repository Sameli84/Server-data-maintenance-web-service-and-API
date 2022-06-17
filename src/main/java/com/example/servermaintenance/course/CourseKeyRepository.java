package com.example.servermaintenance.course;

import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.CourseKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseKeyRepository extends JpaRepository<CourseKey, Long> {
    List<CourseKey> findAll();

    Optional<CourseKey> findCourseKeyByKey(String key);

    boolean existsCourseKeyByKey(String key);

    boolean existsCourseKeyByCourse(Course course);
}
