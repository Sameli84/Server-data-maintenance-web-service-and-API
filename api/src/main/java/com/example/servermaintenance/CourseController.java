package com.example.servermaintenance;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String getCoursePage(@PathVariable String courseUrl, @ModelAttribute("error") String error, Model model) {
        var course = courseService.getCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            // erroria? not found?
            return "redirect:/?error";
        }
        model.addAttribute("error", error);
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
        var course = courseService.newCourseContext(name, url);
        return course.map(value -> "redirect:/c/" + value.getUrl()).orElse("redirect:/create-course?error");
    }

    @PostMapping("/c/{courseUrl}/update-data")
    public String createData(@PathVariable String courseUrl,
                             @RequestParam String studentAlias, @RequestParam String cscUsername, @RequestParam int uid,
                             @RequestParam String dnsName, @RequestParam String selfMadeDnsName,
                             @RequestParam String name, @RequestParam String vpsUsername,
                             @RequestParam String poutaDns, @RequestParam String ipAddress) {

        var account = accountService.getContextAccount();
        var course = courseService.getCourseByUrl(courseUrl);

        if (course.isEmpty()) {
            // erroria? not foundia?
            return "redirect:/?error";
        }
        courseService.updateStudentsData(course.get(), account.get(), studentAlias, cscUsername, uid, dnsName, selfMadeDnsName, name, vpsUsername, poutaDns, ipAddress);

        return "redirect:/c/" + courseUrl;
    }
}
