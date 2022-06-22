package com.example.servermaintenance.course;

import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.SchemaPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SchemaPartRepository extends JpaRepository<SchemaPart, Long> {
    @Query("select sp from SchemaPart sp where sp.course = :course order by sp.order")
    List<SchemaPart> findSchemaPartsOrdered(Course course);
}
