package com.example.servermaintenance.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/course")
public class CourseAPIController {
    @GetMapping("/moi")
    public String getMoi() {
        return "moi";
    }
}
