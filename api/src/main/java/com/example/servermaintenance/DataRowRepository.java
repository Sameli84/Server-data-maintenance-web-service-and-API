package com.example.servermaintenance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataRowRepository extends JpaRepository<DataRow, Long> {
    List<DataRow> findByFirstName(String FirstName);
    List<DataRow> findAll();
}