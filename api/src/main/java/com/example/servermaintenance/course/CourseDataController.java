package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.CourseDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/courses/{course}/data")
public class CourseDataController {
    private final AccountService accountService;
    private final CourseService courseService;
    private final RoleService roleService;

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

    @ModelAttribute("course")
    public Course addCourseToModel(@PathVariable Course course) {
        return course;
    }

    @ModelAttribute("canEdit")
    public boolean addCanEditToModel(@ModelAttribute Course course, @ModelAttribute Account account) {
        return Objects.equals(account.getId(), course.getOwner().getId()) || roleService.isAdmin(account);
    }

    @ModelAttribute("isStudent")
    public boolean addIsStudentToModel(@ModelAttribute Course course, @ModelAttribute Account account) {
        return courseService.isStudentOnCourse(course, account);
    }

    @ModelAttribute("courseDataDto")
    public CourseDataDto addCourseDataDtoToModel(@ModelAttribute Course course) {
        return courseService.getCourseData(course);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean canEdit(Model model) {
        var canEdit = (Boolean) model.getAttribute("canEdit");
        if (canEdit == null) {
            return false;
        }
        return canEdit;
    }

    @GetMapping
    public String getDataTab(@SuppressWarnings("unused") @PathVariable Course course,
                             @ModelAttribute CourseDataDto courseDataDto) {
        return "course/tab-data";
    }

    @PostMapping("/save")
    public String saveEdits(@PathVariable Course course,
                            @ModelAttribute CourseDataDto courseDataDto,
                            Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }
        courseService.saveCourseData(courseDataDto, course);

        return "course/tab-data";
    }

    @PostMapping("/edit")
    public String setEdit(@SuppressWarnings("unused") @PathVariable Course course,
                          @ModelAttribute CourseDataDto courseDataDto,
                          Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }

        return "course/tab-data-edit";
    }

    @PostMapping("/cancel")
    public String cancelEdits(@SuppressWarnings("unused") @PathVariable Course course,
                              Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }
        return "course/tab-data";
    }
}
