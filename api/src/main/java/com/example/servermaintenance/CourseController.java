package com.example.servermaintenance;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class CourseController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private CourseService courseService;

    @GetMapping("/")
    public String getCoursesPage(Model model) {
        var account = accountService.getContextAccount().get();
        model.addAttribute("courses", account.getCourses());
        model.addAttribute("studentCourses", account.getStudentCourses());

        return "courses";
    }

    @GetMapping("/c/{courseUrl}")
    public String getCoursePage(@PathVariable String courseUrl, Model model) {
        var course = courseService.getCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            // erroria? not found?
            return "redirect:/?error";
        }
        model.addAttribute("course", course.get());
        return "course";
    }

    @PostMapping("/c/{courseUrl}/join")
    public String joinCourse(@PathVariable String courseUrl) {
        if (courseService.addToCourseContext(courseUrl)) {
            return "redirect:/c/" + courseUrl;
        } else {
            return "redirect:/?error";
        }
    }

    @GetMapping("/create-course")
    public String getCourseCreationPage() {
        return "create_course";
    }

    @PostMapping("/create-course")
    public String createCourse(@RequestParam String name, @RequestParam String url) {
        try {
            var course = courseService.newCourseContext(name, url);
            return "redirect:/c/" + course.getUrl();
        } catch (Exception e) {
            return "redirect:/create-course?error";
        }
    }
}
