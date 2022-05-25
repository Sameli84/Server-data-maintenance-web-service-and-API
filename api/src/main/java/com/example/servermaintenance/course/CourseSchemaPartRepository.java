package com.example.servermaintenance.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseSchemaPartRepository extends JpaRepository<CourseSchemaPart, Long> {
}
