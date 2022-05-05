package com.example.servermaintenance;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private CourseRepository courseRepository;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/")
    public String getCoursesPage(Model model) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var account = accountRepository.findByEmail(email).get();
        model.addAttribute("courses", account.getCourses());
        model.addAttribute("studentCourses", account.getStudentCourses());

        return "courses";
    }

    @GetMapping("/c/{courseUrl}")
    public String getCoursePage(@PathVariable String courseUrl, Model model) {
        var course = courseRepository.findCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            // erroria?
            return "redirect:/";
        }

        model.addAttribute("course", course.get());
        return "course";
    }

    @PostMapping("/c/{courseUrl}/join")
    public String joinCourse(@PathVariable String courseUrl, Model model) {
        var course = courseRepository.findCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            // erroria?
            return "redirect:/";
        }

        // todo: puhdistusta tai paremmin tämä!
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var account = accountRepository.findByEmail(email).get();
        course.get().addStudent(account);
        courseRepository.save(course.get());
        accountRepository.save(account);
        return "redirect:/c/" + courseUrl;
    }

    @GetMapping("/create-course")
    public String getCourseCreationPage() {
        return "create_course";
    }

    @PostMapping("/create-course")
    public String createCourse(@RequestParam String name, @RequestParam String url) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var account = accountRepository.findByEmail(email).get();
        var c = new Course(name, url,account);

        // TODO: prevent duplicate urls
        courseRepository.save(c);
        return "redirect:/c/" + c.getUrl();
    }
}
