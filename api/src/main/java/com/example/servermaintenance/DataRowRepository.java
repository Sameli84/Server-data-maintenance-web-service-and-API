package com.example.servermaintenance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataRowRepository extends JpaRepository<DataRow, Long> {
    List<DataRow> findAll();
    List<DataRow> findDataRowsByCourse(Course course);
    Optional<DataRow> findDataRowByCourseAndAccount(Course course, Account account);
}