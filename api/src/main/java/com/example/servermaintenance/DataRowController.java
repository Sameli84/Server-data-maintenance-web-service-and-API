package com.example.servermaintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;

@Controller
public class DataRowController {

    @Autowired
    private DataRowService dataRowService;

    @Autowired
    private DataRowRepository repository;

    @GetMapping("/bulkcreate")
    public String bulkcreate(){
// save a single Customer
        repository.save(new DataRow("Raj", "Bhoj"));

// save a list of Customers
        repository.saveAll(Arrays.asList(new DataRow("Salim", "Khan")
                , new DataRow("Rajesh", "Parihar")
                , new DataRow("Rahul", "Dravid")
                , new DataRow("Dharmendra", "Bhojwani")));

        return "Customers are created";
    }

    @GetMapping("/datarowpage")
    public String getDatarows(Model model) {
        ArrayList<DataRow> dataRows = dataRowService.getDataRows();
        model.addAttribute("datarows", dataRows);
        return "datarowpage";
    }

}