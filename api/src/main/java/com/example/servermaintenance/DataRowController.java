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
    private AccountService accountService;

    @Autowired
    private CourseService courseService;

    @GetMapping("/bulkcreate")
    public String bulkcreate() {
        var account = accountService.getContextAccount();
        if (account.isEmpty()) {
            return "redirect:/login";
        }

        var course = courseService.getCourseByUrl("softa");
        if (course.isEmpty()) {
            course = courseService.newCourse("SoftaDevaus", "softa", account.get());
        }

        courseService.updateStudentsData(course.get(), account.get(), "Jakobi", "Juuseri", 55555, "theDNS", "myDNS", "Jaakko", "vpsJuuseri", "8.8.8.8", "123.123.124.12");

        return "redirect:/datarowpage";
    }

    @GetMapping("/datarowpage")
    public String getDatarows(Model model) {
        model.addAttribute("datarows", dataRowService.getDataRows());
        return "datarowpage";
    }
}