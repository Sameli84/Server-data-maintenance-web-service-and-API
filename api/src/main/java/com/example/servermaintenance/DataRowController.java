package com.example.servermaintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class DataRowController {

    @Autowired
    private DataRowService dataRowService;

    @Autowired
    private DataRowRepository dataRowRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/bulkcreate")
    public String bulkcreate(Authentication authentication) {
// save a single Customer

        Account account = accountRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).get();

        System.out.println(account.toString());

        Course course = new Course("SoftaDev", "www.tuni.fi", account);
        courseRepository.save(course);
        dataRowRepository.save(new DataRow("Jakobi", "Juuseri", 55555, "theDNS", "myDNS", "Jaakko", "vpsJuuseri", "8.8.8.8", "123.123.124.12", account, course));

    /*
        repository.saveAll(Arrays.asList(new DataRow("Salim", "Khan")
                , new DataRow("Rajesh", "Parihar")
                , new DataRow("Rahul", "Dravid")
                , new DataRow("Dharmendra", "Bhojwani")));

    */

        return "redirect:/datarowpage";
    }

    @GetMapping("/datarowpage")
    public String getDatarows(Model model) {
        List<DataRow> dataRows = dataRowService.getDataRows();
        model.addAttribute("datarows", dataRows);
        return "datarowpage";
    }

}