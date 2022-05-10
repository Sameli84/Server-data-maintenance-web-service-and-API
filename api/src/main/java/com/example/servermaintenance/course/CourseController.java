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

import java.util.Optional;

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

    @Autowired
    private CourseKeyRepository courseKeyRepository;

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
        model.addAttribute("hasKey", course.get().getCourseKeys().size() > 0);
        return "course";
    }

    @PostMapping("/courses/{courseUrl}/join")
    public String joinCourse(@PathVariable String courseUrl, @RequestParam Optional<String> key) {
        if (courseService.joinToCourseContext(courseUrl, key.orElse(""))) {
            return "redirect:/courses/" + courseUrl + "?joined";
        } else {
            return "redirect:/courses/" + courseUrl + "?error";
        }
    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/courses/{courseUrl}/students/{studentId}/kick")
    public String kickFromCourse(@PathVariable String courseUrl, @PathVariable int studentId) {
        var contextUser = accountService.getContextAccount().get();
        var course = courseService.getCourseByUrl(courseUrl);
        var redirect = "redirect:/courses/" + courseUrl;
        var redirectError = redirect + "?error";
        if (course.isEmpty()) {
            return redirectError;
        }
        if (course.get().getOwner().getId().intValue() != contextUser.getId().intValue()) {
            return redirectError;
        }

        Account account = accountService.getAccountById(studentId);
        if (courseService.kickFromCourse(course.get(), account)) {
            return redirect;
        } else {
            return redirectError;
        }
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/courses/create")
    public String getCourseCreationPage() {
        return "create_course";
    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/courses/create")
    public String createCourse(@RequestParam String name, @RequestParam String url, @RequestParam String key) {
        var course = courseService.newCourseContext(name, url);
        if (course.isPresent()) {
            if (!key.isEmpty()) {
                courseKeyRepository.save(new CourseKey(key, course.get()));
            }
            return "redirect:/courses/" + course.get().getUrl();
        } else {
            return "redirect:/courses/create?error";
        }
    }

    @PostMapping("/courses/{courseUrl}/students/{studentId}/update-data")
    public String createData(@PathVariable String courseUrl, @PathVariable int studentId,
                             @RequestParam String studentAlias, @RequestParam String cscUsername, @RequestParam int uid,
                             @RequestParam String dnsName, @RequestParam String selfMadeDnsName,
                             @RequestParam String name, @RequestParam String vpsUsername,
                             @RequestParam String poutaDns, @RequestParam String ipAddress, RedirectAttributes ra) {

        var account = accountService.getContextAccount().get();
        if (account.getId() != studentId && !roleService.isTeacher(account)) {
            return "redirect:/courses/" + courseUrl + "?error";
        }

        var course = courseService.getCourseByUrl(courseUrl);

        if (course.isEmpty()) {
            // erroria? not foundia?
            return "redirect:/courses/" + courseUrl + "?error";
        }

        Boolean check = courseService.checkIfStudentOnCourse(course.get(), account);
        if (!check) {
            ra.addFlashAttribute("error", "You must sign up for course to create projects!");
            return "redirect:/courses/" + courseUrl;
        }

        var data = dataRowService.getStudentData(course.get(), account);
        if (data.isEmpty()) {
            courseService.updateStudentsData(new DataRow(studentAlias, cscUsername, uid, dnsName, selfMadeDnsName, name, vpsUsername, poutaDns, ipAddress, account, course.get()));
        } else {
            data.get().update(studentAlias, cscUsername, uid, dnsName, selfMadeDnsName, name, vpsUsername, poutaDns, ipAddress);
            courseService.updateStudentsData(data.get());
        }

        return "redirect:/courses/" + course.get().getUrl();
    }

    @PostMapping("/courses/join")
    public String joinCourseByKey(@RequestParam String key) {
        var courseKey = courseKeyRepository.findCourseKeyByKey(key);
        if (courseKey.isEmpty()) {
            return "redirect:/courses?error";
        }
        var courseUrl = courseKey.get().getCourse().getUrl();
        if (courseService.joinToCourseContext(courseUrl, key)) {
            return "redirect:/courses/" + courseUrl + "?joined";
        } else {
            return "redirect:/courses/" + courseUrl + "?error";
        }
    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/courses/{courseUrl}/keys/create")
    public String createCourseKey(@PathVariable String courseUrl, @RequestParam String key) {
        var course = courseService.getCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            return "redirect:/courses?error";
        }
        if (courseService.addKey(course.get(), key)) {
            return "redirect:/courses/" + courseUrl + "?key";
        } else {
            return "redirect:/courses/" + courseUrl + "?error";
        }
    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/courses/{courseUrl}/keys/{keyId}/revoke")
    public String revokeCourseKey(@PathVariable String courseUrl, @PathVariable int keyId) {
        var course = courseService.getCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            return "redirect:/courses?error";
        }

        if (courseService.deleteKey(course.get(), keyId)) {
            return "redirect:/courses/" + courseUrl;
        } else {
            return "redirect:/courses/" + courseUrl + "?error";
        }
    }
}
