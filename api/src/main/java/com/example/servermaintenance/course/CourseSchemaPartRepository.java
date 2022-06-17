package com.example.servermaintenance.course;

import com.example.servermaintenance.course.domain.SchemaPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseSchemaPartRepository extends JpaRepository<SchemaPart, Long> {
}
