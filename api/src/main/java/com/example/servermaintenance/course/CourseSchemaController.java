package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.AccountRepository;
import com.example.servermaintenance.account.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Secured("ROLE_TEACHER")
@Controller
@SessionAttributes("courseSchemaDto")
@AllArgsConstructor
public class CourseSchemaController {
    private final CourseDataPartRepository courseDataPartRepository;
    private final CourseService courseService;
    private final AccountService accountService;

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

    @ModelAttribute("account")
    public Account addAccountToModel() throws AccountNotFoundException {
        return accountService.getContextAccount().orElseThrow(AccountNotFoundException::new);
    }

    @ModelAttribute(name = "courseSchemaDto")
    public CourseSchemaDto schema() {
        var courseSchemaDto = new CourseSchemaDto();
        courseSchemaDto.addPart(new CourseSchemaPartDto());
        return courseSchemaDto;
    }

    @GetMapping("/courses/schema")
    public String showCourseSchemaPage(@ModelAttribute CourseSchemaDto courseSchemaDto) {
        if (courseSchemaDto.getCourseName() == null || courseSchemaDto.getCourseName().isEmpty()) {
            return "redirect:/courses/create";
        }
        return "schema/create";
    }

    @PostMapping("/courses/schema")
    public String createCourseSchema(@ModelAttribute CourseSchemaDto courseSchemaDto, @ModelAttribute Account account, SessionStatus sessionStatus) {
        var course = courseService.createCourse(courseSchemaDto, account);
        sessionStatus.setComplete();
        return "redirect:/courses/" + course.getUrl();
    }

    @GetMapping("/courses/create")
    public String showCourseCreationPage() {
        return "schema/create-course";
    }

    @PostMapping("/courses/create")
    public String saveCourseCreationData(@ModelAttribute CourseSchemaDto courseSchemaDto) {
        return "redirect:/courses/schema";
    }

    @GetMapping("/schema/parts/add")
    public String addPartToSchema(CourseSchemaPartDto part, @ModelAttribute CourseSchemaDto courseSchemaDto) {
        courseSchemaDto.addPart(part);
        return "schema/create :: #schemaForm";
    }

    @DeleteMapping("/schema/parts/{index}/delete")
    public String deletePartFromSchema(@PathVariable int index, @ModelAttribute CourseSchemaDto courseSchemaDto) {
        courseSchemaDto.getParts().remove(index);
        return "schema/create :: #schemaForm";
    }

    @PostMapping("/schema/render")
    public String renderSchema(@ModelAttribute CourseSchemaDto courseSchemaDto) {
        return "schema/create :: #render";
    }

    @GetMapping("/schema/courses/{course}")
    public String getCoursePage(@PathVariable Course course, Model model) {
        var rows = course.getCourseStudentData()
                .stream()
                .sorted(Comparator.comparingLong(CourseStudentData::getId))
                .map(courseDataPartRepository::findCourseDataPartsByCourseStudentDataOrderByCourseSchemaPart_Order)
                .toList();

        var headers = course.getCourseSchemaParts()
                .stream()
                .sorted(Comparator.comparingInt(CourseSchemaPart::getOrder))
                .map(CourseSchemaPart::getName)
                .toList();

        model.addAttribute("headers", headers);
        model.addAttribute("rows", rows);
        return "schema/course";
    }
}
