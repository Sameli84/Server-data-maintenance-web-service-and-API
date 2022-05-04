package com.example.servermaintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class DataRowService {

    @Autowired
    private DataRowRepository dataRowRepository;

    public ArrayList<DataRow> getDataRows() { return this.dataRowRepository.findAll(); }
}
