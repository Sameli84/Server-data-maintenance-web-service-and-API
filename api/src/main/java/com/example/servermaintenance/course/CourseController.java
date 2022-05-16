package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.datarow.DataRow;
import com.example.servermaintenance.datarow.DataRowService;
import com.example.servermaintenance.account.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@AllArgsConstructor
public class CourseController {
    private final AccountService accountService;
    private final CourseService courseService;
    private final DataRowService dataRowService;
    private final RoleService roleService;
    private final CourseKeyRepository courseKeyRepository;

    @ExceptionHandler(AccountNotFoundException.class)
    public String processAccountException(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login";
    }

    // TODO: specific exception for courses!
    @ExceptionHandler(NoSuchElementException.class)
    public String processCourseNotFoundException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Course not found");
        return "redirect:/courses";
    }

    @ModelAttribute("user")
    public Account addAccountToModel() throws AccountNotFoundException {
        return accountService.getContextAccount().orElseThrow(AccountNotFoundException::new);
    }

    @GetMapping("/")
    public String getIndexPage() {
        return "redirect:/courses";
    }

    @GetMapping("/courses")
    public String getCoursesPage(@ModelAttribute Account user, Model model) {
        var courses = new HashSet<>(user.getStudentCourses());

        var userCourses = user.getCourses();
        if (userCourses != null) {
            courses.addAll(userCourses);
        }

        model.addAttribute("courses", courses);

        return "courses";
    }

    @PostMapping("/courses/join")
    public String joinCourseByKey(@ModelAttribute Account user, @RequestParam String key, RedirectAttributes redirectAttributes) {
        var courseKey = courseKeyRepository.findCourseKeyByKey(key);
        if (courseKey.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Given course key not found!");
            return "redirect:/courses";
        }
        var course = courseKey.get().getCourse();
        if (courseService.joinToCourse(course, user, key)) {
            redirectAttributes.addFlashAttribute("success", "Joined course");
            return "redirect:/courses/" + course.getUrl();
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to join course");
            return "redirect:/courses";
        }
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/courses/create")
    public String getCourseCreationPage() {
        return "create_course";
    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/courses/create")
    public String createCourse(@ModelAttribute Account user, @RequestParam String name, @RequestParam String url, @RequestParam String key, RedirectAttributes redirectAttributes) {
        var course = courseService.newCourse(name, url, user, key);
        if (course.isPresent()) {
            return "redirect:/courses/" + course.get().getUrl();
        } else {
            redirectAttributes.addFlashAttribute("error", "Couldn't create a new course");
            return "redirect:/courses/create";
        }
    }

    @GetMapping("/courses/{course}")
    public String getCoursePage(@PathVariable Course course, @ModelAttribute Account user, Model model) {
        var data = dataRowService.getStudentData(course, user);

        model.addAttribute("data", data.orElse(null));
        model.addAttribute("datarows", dataRowService.getCourseData(course));
        model.addAttribute("isStudent", course.getStudents().contains(user));
        model.addAttribute("hasKey", course.getCourseKeys().size() > 0);
        return "course";
    }

    @PostMapping("/courses/{course}/join")
    public String joinCourse(@PathVariable Course course, @RequestParam Optional<String> key, @ModelAttribute Account user, RedirectAttributes redirectAttributes) {
        if (courseService.joinToCourse(course, user, key.orElse(""))) {
            redirectAttributes.addFlashAttribute("success", "Joined course");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to join course " + course.getName());
        }
        return "redirect:/courses/" + course.getUrl();
    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/courses/{course}/students/{studentId}/kick")
    public String kickFromCourse(@PathVariable Course course, @PathVariable int studentId, @ModelAttribute Account user, RedirectAttributes redirectAttributes) {
        if (!Objects.equals(course.getOwner().getId(), user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized action");
            return "redirect:/courses";
        }

        var student = accountService.getAccountById(studentId);
        if (student == null) {
            redirectAttributes.addFlashAttribute("error", "Student not found");
        } else {
            if (!courseService.kickFromCourse(course, student)) {
                redirectAttributes.addFlashAttribute("error", "Couldn't kick user from the course");
            }
        }
        return "redirect:/courses/" + course.getUrl();
    }

    @PostMapping("/courses/{course}/students/{studentId}/update-data")
    public String createData(@PathVariable Course course, @PathVariable Long studentId, @ModelAttribute Account user,
                             @RequestParam String cscUsername, @RequestParam int uid,
                             @RequestParam String dnsName, @RequestParam String selfMadeDnsName,
                             @RequestParam String name, @RequestParam String vpsUsername,
                             @RequestParam String poutaDns, @RequestParam String ipAddress, RedirectAttributes redirectAttributes) {

        if (!Objects.equals(user.getId(), studentId) && !roleService.isTeacher(user)) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized action");
            return "redirect:/courses/" + course;
        }

        String studentAlias = user.getEmail().split("@")[0];


        if (!courseService.checkIfStudentOnCourse(course, user)) {
            redirectAttributes.addFlashAttribute("error", "You must sign up for course before submitting data!");
            return "redirect:/courses/" + course;
        }

        var data = dataRowService.getStudentData(course, user);
        if (data.isEmpty()) {
            courseService.updateStudentsData(new DataRow(studentAlias, cscUsername, uid, dnsName, selfMadeDnsName, name, vpsUsername, poutaDns, ipAddress, user, course));
        } else {
            data.get().update(studentAlias, cscUsername, uid, dnsName, selfMadeDnsName, name, vpsUsername, poutaDns, ipAddress);
            courseService.updateStudentsData(data.get());
        }

        return "redirect:/courses/" + course.getUrl();
    }


    @Secured("ROLE_TEACHER")
    @PostMapping("/courses/{course}/keys/create")
    public String createCourseKey(@PathVariable Course course, @RequestParam String key, @ModelAttribute Account user, RedirectAttributes redirectAttributes) {
        if (courseService.addKey(course, key)) {
            redirectAttributes.addFlashAttribute("success", "New key created");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to create a new key");
        }
        return "redirect:/courses/" + course.getUrl();
    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/courses/{course}/keys/{keyId}/revoke")
    public String revokeCourseKey(@PathVariable Course course, @PathVariable int keyId, @ModelAttribute Account user, RedirectAttributes redirectAttributes) {
        if (!courseService.deleteKey(course, keyId)) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete the key");
        }
        return "redirect:/courses/" + course.getUrl();
    }
}
