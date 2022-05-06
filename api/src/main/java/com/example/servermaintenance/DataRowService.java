package com.example.servermaintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DataRowService {

    @Autowired
    private DataRowRepository dataRowRepository;

    public List<DataRow> getDataRows() {
        return this.dataRowRepository.findAll();
    }

    public Optional<DataRow> getStudentData(Course course, Account account) {
        return dataRowRepository.findDataRowByCourseAndAccount(course, account);
    }

    public List<DataRow> getCourseData(Course course) {
        return dataRowRepository.findDataRowsByCourse(course);
    }
}
