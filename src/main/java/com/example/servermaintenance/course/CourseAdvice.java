package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.course.domain.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@ControllerAdvice(assignableTypes = {
        CourseController.class,
        CourseDataController.class,
        CourseSchemaController.class
})
@RequiredArgsConstructor
public class CourseAdvice {
    private final CourseService courseService;
    private final RoleService roleService;

    @ExceptionHandler(CourseNotFoundException.class)
    public String processCourseNotFoundException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Course not found");
        return "redirect:/courses";
    }

    @ModelAttribute("course")
    public Course addCourseToModel(@PathVariable String courseUrl) throws CourseNotFoundException {
        return courseService.getCourseByUrl(courseUrl).orElseThrow(CourseNotFoundException::new);
    }

    @ModelAttribute("isStudent")
    public boolean addIsStudentToModel(@ModelAttribute Course course, @ModelAttribute Account account) {
        return courseService.isStudentOnCourse(course, account);
    }

    @ModelAttribute("canEdit")
    public boolean addCanEditToModel(@ModelAttribute Course course, @ModelAttribute Account account) {
        return Objects.equals(account.getId(), course.getOwner().getId()) || roleService.isAdmin(account);
    }
}
