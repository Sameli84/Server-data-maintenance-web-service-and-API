package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.course.domain.CourseCreationDto;
import com.example.servermaintenance.course.domain.CourseStudent;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.modelmapper.internal.util.ToStringBuilder;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashSet;
import java.util.stream.Collectors;

@AllArgsConstructor
@Controller
public class CoursesController {
    private final CourseKeyRepository courseKeyRepository;
    private final CourseService courseService;
    private final AccountService accountService;

    @ExceptionHandler(AccountNotFoundException.class)
    public String processAccountException(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/courses";
    }

    @GetMapping("/")
    public String getIndexPage() {
        return "redirect:/courses";
    }

    @GetMapping("/courses")
    public String getCoursesPage(@ModelAttribute Account account, Model model, Principal principal) {
        var courses = account.getCourseStudentData().stream().map(CourseStudent::getCourse).collect(Collectors.toCollection(HashSet::new));

        var userCourses = account.getCourses();
        if (userCourses != null) {
            courses.addAll(userCourses);
        }

        model.addAttribute("courses", courses);

        return "courses";
    }

    @PostMapping("/courses/join")
    public String joinCourseByKey(@ModelAttribute Account account, @RequestParam String key, RedirectAttributes redirectAttributes) {
        var courseKey = courseKeyRepository.findCourseKeyByKey(key);
        if (courseKey.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Course with the given key not found!");
            return "redirect:/courses";
        }
        var course = courseKey.get().getCourse();
        if (courseService.joinToCourse(course, account, key)) {
            redirectAttributes.addFlashAttribute("success", "Joined course");
            return "redirect:/courses/" + course.getUrl();
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to join course");
            return "redirect:/courses";
        }
    }

    @RolesAllowed("TEACHER")
    @GetMapping("/courses/create")
    public String showCourseCreationPage(Model model) {
        model.addAttribute("courseCreationDto", new CourseCreationDto());
        return "course/create-course";
    }

    @RolesAllowed("TEACHER")
    @PostMapping("/courses/create")
    public String createCourse(@ModelAttribute CourseCreationDto courseCreationDto, @ModelAttribute Account account) {
        var course = courseService.createCourse(courseCreationDto, account);
        return String.format("redirect:/courses/%s/schema", course.getUrl());
    }
}
