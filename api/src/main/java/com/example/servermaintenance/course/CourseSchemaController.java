package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.course.domain.CourseSchemaDto;
import com.example.servermaintenance.course.domain.CourseSchemaPartDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;

@Slf4j
@Secured("ROLE_TEACHER")
@Controller
@SessionAttributes("courseSchemaDto")
@AllArgsConstructor
public class CourseSchemaController {
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
        return "course/create-schema";
    }

    @PostMapping("/courses/schema")
    public String createCourseSchema(@ModelAttribute CourseSchemaDto courseSchemaDto, @ModelAttribute Account account, SessionStatus sessionStatus) {
        var course = courseService.createCourse(courseSchemaDto, account);
        sessionStatus.setComplete();
        return "redirect:/courses/" + course.getUrl();
    }

    @GetMapping("/courses/create")
    public String showCourseCreationPage() {
        return "course/create-course";
    }

    @PostMapping("/courses/create")
    public String saveCourseCreationData(@ModelAttribute CourseSchemaDto courseSchemaDto) {
        return "redirect:/courses/schema";
    }

    @GetMapping("/courses/schema/parts/add")
    public String addPartToSchema(CourseSchemaPartDto part, @ModelAttribute CourseSchemaDto courseSchemaDto) {
        courseSchemaDto.addPart(part);
        return "course/create-schema :: #schemaForm";
    }

    @DeleteMapping("/courses/schema/parts/{index}/delete")
    public String deletePartFromSchema(@PathVariable int index, @ModelAttribute CourseSchemaDto courseSchemaDto) {
        courseSchemaDto.getParts().remove(index);
        return "course/create-schema :: #schemaForm";
    }

    @PostMapping("/courses/schema/render")
    public String renderSchema(@ModelAttribute CourseSchemaDto courseSchemaDto) {
        return "course/create-schema :: #render";
    }

}
