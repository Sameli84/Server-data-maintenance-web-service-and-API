package com.example.servermaintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataRowService {

    @Autowired
    private DataRowRepository dataRowRepository;

    public List<DataRow> getDataRows() { return this.dataRowRepository.findAll(); }

    public List<DataRow> getDataRowsByCourse(Course course) {
        return this.dataRowRepository.findAllByCourse(course);
    }
}
