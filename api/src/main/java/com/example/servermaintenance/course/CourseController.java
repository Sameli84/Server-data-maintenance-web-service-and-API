package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.datarow.DataRow;
import com.example.servermaintenance.datarow.DataRowService;
import com.example.servermaintenance.account.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
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

    @Autowired
    private RoleService roleService;

    @GetMapping("/")
    public String getIndexPage() {
        return "redirect:/courses";
    }

    @GetMapping("/courses")
    public String getCoursesPage(Model model) {
        var account = accountService.getContextAccount().get();
        model.addAttribute("courses", account.getCourses());
        model.addAttribute("studentCourses", account.getStudentCourses());

        return "courses";
    }

    @GetMapping("/courses/{courseUrl}")
    public String getCoursePage(@PathVariable String courseUrl, @ModelAttribute("error") String error, Model model) {
        var course = courseService.getCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            // erroria? not found?
            return "redirect:/courses?error";
        }
        var account = accountService.getContextAccount().get();

        var data = dataRowService.getStudentData(course.get(), account);

        model.addAttribute("error", error);
        model.addAttribute("course", course.get());
        model.addAttribute("data", data.orElse(null));
        model.addAttribute("datarows", dataRowService.getCourseData(course.get()));
        model.addAttribute("user", account);
        model.addAttribute("isStudent", course.get().getStudents().contains(account));
        return "course";
    }

    @PostMapping("/courses/{courseUrl}/join")
    public String joinCourse(@PathVariable String courseUrl) {
        if (courseService.joinToCourseContext(courseUrl)) {
            return "redirect:/courses/" + courseUrl;
        } else {
            return "redirect:/courses/" + courseUrl + "?error";
        }
    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/courses/{courseUrl}/{accountId}/kick")
    public String kickFromCourse(@PathVariable String courseUrl, @PathVariable Long accountId) {
        Account account = accountService.getAccountById(accountId);
        if (courseService.kickFromCourse(courseUrl, account)) {
            return "redirect:/courses/" + courseUrl;
        } else {
            return "redirect:/courses/" + courseUrl + "?error";
        }
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/courses/create")
    public String getCourseCreationPage() {
        return "create_course";
    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/courses/create")
    public String createCourse(@RequestParam String name, @RequestParam String url) {
        var course = courseService.newCourseContext(name, url);
        return course.map(value -> "redirect:/courses/" + value.getUrl()).orElse("redirect:/courses/create?error");
    }

    @PostMapping("/courses/{courseUrl}/students/{studentId}/update-data")
    public String createData(@PathVariable String courseUrl, @PathVariable int studentId,
                             @RequestParam String studentAlias, @RequestParam String cscUsername, @RequestParam int uid,
                             @RequestParam String dnsName, @RequestParam String selfMadeDnsName,
                             @RequestParam String name, @RequestParam String vpsUsername,
                             @RequestParam String poutaDns, @RequestParam String ipAddress, RedirectAttributes ra) {

        var account = accountService.getContextAccount();
        if (account.get().getId() != studentId && !roleService.isTeacher(account.get())) {
            return "redirect:/courses/" + courseUrl + "?error";
        }

        var course = courseService.getCourseByUrl(courseUrl);

        if (course.isEmpty()) {
            // erroria? not foundia?
            return "redirect:/courses?error";
        }

        Boolean check = courseService.checkIfStudentOnCourse(course.get(), account.get());
        if(!check) {
            ra.addFlashAttribute("error", "You must sign up for course to create projects!");
            return "redirect:/courses/" + courseUrl;
        }

        var data = dataRowService.getStudentData(course.get(), account.get());
        if (data.isEmpty()) {
            courseService.updateStudentsData(new DataRow(studentAlias, cscUsername, uid, dnsName, selfMadeDnsName, name, vpsUsername, poutaDns, ipAddress, account.get(), course.get()));
        } else {
            data.get().update(studentAlias, cscUsername, uid, dnsName, selfMadeDnsName, name, vpsUsername, poutaDns, ipAddress);
            courseService.updateStudentsData(data.get());
        }

        return "redirect:/courses/" + course.get().getUrl();
    }
}
