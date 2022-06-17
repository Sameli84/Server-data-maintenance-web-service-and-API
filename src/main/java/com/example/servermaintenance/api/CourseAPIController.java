package com.example.servermaintenance.api;

import com.example.servermaintenance.course.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "/api/course")
public class CourseAPIController {
    @Autowired
    private CourseService courseService;

    @GetMapping("/moi")
    public String getMoi() {
        return "moi";
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/{courseUrl}")
    public void generateReport(@PathVariable String courseUrl, HttpServletResponse response) {
        try {
            response.setStatus(200); // OK
            response.setHeader("Content-Disposition", "attachment; filename=" + courseUrl + ".csv");
            response.setContentType("text/csv");
            courseService.writeReportContext(courseUrl, response.getWriter());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "unable generate report " + courseUrl, e);
        }
    }
}
