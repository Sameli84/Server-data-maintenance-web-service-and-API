package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.CourseDataInputDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes("courseSessionMap")
@RequestMapping("/courses/{course}/data")
public class CourseDataInputController {
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

    @ModelAttribute("courseSessionMap")
    public CourseSessionMap<CourseDataInputDto> addCourseSessionMapToModel() {
        return new CourseSessionMap<>();
    }

    @ModelAttribute("courseDataInputDto")
    public CourseDataInputDto addCourseDataInputDtoToModel(@ModelAttribute CourseSessionMap<CourseDataInputDto> courseSessionMap,
                                                           @ModelAttribute Course course) {
        return courseSessionMap.get(course, () -> courseService.getCourseDataForm(course));
    }

    @ModelAttribute("isStudent")
    public boolean addIsStudentToModel(@ModelAttribute Course course, @ModelAttribute Account account) {
        return courseService.isStudentOnCourse(course, account);
    }

    @ModelAttribute("canEdit")
    public boolean addCanEditToModel(@ModelAttribute Course course, @ModelAttribute Account account) {
        return Objects.equals(account.getId(), course.getOwner().getId()) || roleService.isAdmin(account);
    }

    @GetMapping
    public String getDataTab(@PathVariable Course course, @ModelAttribute CourseDataInputDto courseDataInputDto, Model model) {
        if (courseDataInputDto.isEdit()) {
            return "course/tab-data-edit";
        } else {
            model.addAttribute("courseData", courseService.getCourseData(course));
            return "course/tab-data";
        }
    }

    @PostMapping("/save")
    public String saveEdits(@PathVariable Course course,
                            @ModelAttribute CourseSessionMap<CourseDataInputDto> courseSessionMap,
                            @ModelAttribute CourseDataInputDto courseDataInputDto,
                            Model model) {
        courseService.saveCourseDataInput(courseDataInputDto);
        model.addAttribute("courseData", courseDataInputDto.getCourseDataDto());
        courseSessionMap.remove(course);

        return "course/tab-data";
    }

    @PostMapping("/edit")
    public String setEdit(@PathVariable Course course, @ModelAttribute CourseDataInputDto courseDataInputDto, Model model) {
        courseDataInputDto.setEdit(true);
        return getDataTab(course, courseDataInputDto, model);
    }

    @PostMapping("/cancel")
    public String cancelEdits(@PathVariable Course course, @ModelAttribute CourseSessionMap<CourseDataInputDto> courseSessionMap, Model model) {
        var courseDataInputDto = courseSessionMap.add(course, courseService.getCourseDataForm(course));
        return getDataTab(course, courseDataInputDto, model);
    }

    @PutMapping("/rows/{row}/parts/{part}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateField(@SuppressWarnings("unused") @PathVariable Course course,
                            @PathVariable int row,
                            @PathVariable int part,
                            @RequestParam String value,
                            @ModelAttribute CourseDataInputDto courseDataInputDto) {
        var parts = courseDataInputDto.getCourseDataDto().getRows().get(row).getParts();

        var studentPart = parts.get(part);
        studentPart.setData(value);
        parts.set(part, studentPart);
    }
}
