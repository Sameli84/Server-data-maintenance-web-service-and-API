package com.example.servermaintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
public class DataRowController {
    @Autowired
    DataRowRepository repository;

    @GetMapping("/bulkcreate")
    public String bulkcreate(){
// save a single Customer
        repository.save(new DataRow("Raj", "Bhojwani"));

// save a list of Customers
        repository.saveAll(Arrays.asList(new DataRow("Salim", "Khan")
                , new DataRow("Rajesh", "Parihar")
                , new DataRow("Rahul", "Dravid")
                , new DataRow("Dharmendra", "Bhojwani")));

        return "Customers are created";
    }

}