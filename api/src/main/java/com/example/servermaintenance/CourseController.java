package com.example.servermaintenance;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
public class CourseController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private DataRowService dataRowService;

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
        var account = accountService.getContextAccount().get();

        var data = dataRowService.getStudentData(course.get(), account);

        model.addAttribute("error", error);
        model.addAttribute("course", course.get());
        model.addAttribute("data", data.orElse(null));
        model.addAttribute("datarows", dataRowService.getCourseData(course.get()));
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
                             @RequestParam String poutaDns, @RequestParam String ipAddress, RedirectAttributes ra) {

        var account = accountService.getContextAccount();
        var course = courseService.getCourseByUrl(courseUrl);

        if (course.isEmpty()) {
            // erroria? not foundia?
            return "redirect:/?error";
        }

        Boolean check = courseService.checkIfStudentOnCourse(course.get(), account.get());
        if(!check) {
            ra.addFlashAttribute("error", "You must sign up for course to create projects!");
            return "redirect:/c/" + courseUrl;
        }

        var data = dataRowService.getStudentData(course.get(), account.get());
        if (data.isEmpty()) {
            courseService.updateStudentsData(new DataRow(studentAlias, cscUsername, uid, dnsName, selfMadeDnsName, name, vpsUsername, poutaDns, ipAddress, account.get(), course.get()));
        } else {
            data.get().update(studentAlias, cscUsername, uid, dnsName, selfMadeDnsName, name, vpsUsername, poutaDns, ipAddress);
            courseService.updateStudentsData(data.get());
        }

        return "redirect:/c/" + course.get().getUrl();
    }
}
