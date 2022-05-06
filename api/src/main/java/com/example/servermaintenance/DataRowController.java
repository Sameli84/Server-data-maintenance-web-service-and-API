package com.example.servermaintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class DataRowController {

    @Autowired
    private DataRowService dataRowService;

    @Autowired
    private CourseService courseService;

    @GetMapping("/datarowpage")
    public String getDatarows(Model model) {
        model.addAttribute("datarows", dataRowService.getDataRows());
        model.addAttribute("courses", courseService.getCourses());
        return "datarowpage";
    }

    @PostMapping("/datarowpage")
    public String filterDatarows(@RequestParam Long selectCourse, Model model) {
        System.out.println(selectCourse);
        List<DataRow> dataRows;
        if(selectCourse == -1) {
            dataRows = dataRowService.getDataRows();
        } else {
            Course course = courseService.getCourseById(selectCourse);
            dataRows = dataRowService.getCourseData(course);
        }
        List<Course> courses = courseService.getCourses();
        model.addAttribute("datarows", dataRows);
        model.addAttribute("courses", courses);
        return "datarowpage";
    }
}