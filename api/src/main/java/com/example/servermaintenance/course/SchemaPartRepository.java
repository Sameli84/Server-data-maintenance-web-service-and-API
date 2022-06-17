package com.example.servermaintenance.course;

import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.SchemaPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchemaPartRepository extends JpaRepository<SchemaPart, Long> {
    List<SchemaPart> findSchemaPartsByCourseOrderByOrder(Course course);
}
