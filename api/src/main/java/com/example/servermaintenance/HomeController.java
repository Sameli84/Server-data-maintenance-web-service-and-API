package com.example.servermaintenance;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@AllArgsConstructor
public class HomeController {
    @GetMapping("/")
    public static String getHome() {
        return "index";
    }
}
