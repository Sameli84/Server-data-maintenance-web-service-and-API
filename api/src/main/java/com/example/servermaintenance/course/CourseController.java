package com.example.servermaintenance.course;

import com.example.servermaintenance.AlertService;
import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.course.domain.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

@Slf4j
@Controller
@AllArgsConstructor
@RequestMapping("/courses/{course}")
public class CourseController {
    private final AccountService accountService;
    private final CourseService courseService;
    private final RoleService roleService;
    private final CourseStudentPartRepository courseStudentPartRepository;
    private final CourseStudentService courseStudentService;
    private final ModelMapper modelMapper;
    private final AlertService alertService;

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

    @ModelAttribute("isStudent")
    public boolean addIsStudentToModel(@ModelAttribute Course course, @ModelAttribute Account account) {
        return courseService.isStudentOnCourse(course, account);
    }

    @ModelAttribute("canEdit")
    public boolean addCanEditToModel(@ModelAttribute Course course, @ModelAttribute Account account) {
        return Objects.equals(account.getId(), course.getOwner().getId()) || roleService.isAdmin(account);
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
    public String getCoursePage(@PathVariable Course course, @ModelAttribute Account account, Model model) {
        var rows = course.getCourseStudents()
                .stream()
                .sorted(Comparator.comparingLong(CourseStudent::getId))
                .map(courseStudentPartRepository::findCourseStudentPartsByCourseStudentOrderBySchemaPart_Order)
                .toList();

        var headers = course.getSchemaParts()
                .stream()
                .sorted(Comparator.comparingInt(SchemaPart::getOrder))
                .map(SchemaPart::getName)
                .toList();

        model.addAttribute("headers", headers);
        model.addAttribute("rows", rows);

        model.addAttribute("hasKey", courseService.hasCourseKey(course));

        if (courseService.isStudentOnCourse(course, account)) {
            var studentForm = courseService.getStudentForm(course, account);
            model.addAttribute("courseSchemaInputDto", studentForm);
            model.addAttribute("updateLocked", studentForm.getParts().stream().allMatch(CourseSchemaPartDto::isLocked));
        }

        return "course/page";
    }

    @PostMapping("/join")
    public String joinCourse(@PathVariable Course course, @RequestParam Optional<String> key, @ModelAttribute Account account, RedirectAttributes redirectAttributes) {
        if (courseService.joinToCourse(course, account, key.orElse(""))) {
            redirectAttributes.addFlashAttribute("success", "Joined course");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to join course " + course.getName());
        }
        return "redirect:/courses/" + course.getUrl();
    }

    @Secured("ROLE_TEACHER")
    @DeleteMapping("/delete")
    public void deleteCourse(@PathVariable Course course, @ModelAttribute Account account, HttpServletResponse response, Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        if (!courseService.deleteCourse(course, account)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete the course");
        } else {
            response.addHeader("HX-Redirect", "/courses/");
        }
    }

    @GetMapping("/input")
    public String getInputTab(@PathVariable Course course, @ModelAttribute Account account, Model model) {
        if (!courseService.isStudentOnCourse(course, account)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        var studentForm = courseService.getStudentForm(course, account);
        model.addAttribute("courseSchemaInputDto", studentForm);
        model.addAttribute("updateLocked", studentForm.getParts().stream().allMatch(CourseSchemaPartDto::isLocked));

        return "course/tab-input";
    }

    @PostMapping("/students/{studentId}/update")
    public String createData(@PathVariable Course course,
                             @PathVariable Long studentId,
                             @ModelAttribute Account account,
                             @Valid @ModelAttribute CourseSchemaInputDto courseSchemaInputDto,
                             Model model,
                             HttpServletResponse response) {
        if (!Objects.equals(account.getId(), studentId) && !canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }

        if (!courseService.isStudentOnCourse(course, account)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must sign up for course before submitting data");
        }

        var studentParts = courseStudentService.getCourseStudentParts(course, account);

        // remove locked parts at the end from the count
        int studentPartCount = studentParts.size();
        for (int i = studentParts.size() - 1; i >= 0; i--) {
            var part = studentParts.get(i);
            if (part.getSchemaPart().isLocked()) {
                studentPartCount--;
            } else {
                break;
            }
        }

        if (studentPartCount == 0) {
            // TODO: better error message
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No open fields");
        }

        courseSchemaInputDto.setErrors(new HashMap<>());
        courseSchemaInputDto.setParts(new ArrayList<>());

        var dataParts = courseSchemaInputDto.getData();
        if (dataParts.size() != studentPartCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong amount of data parts");
        }

        // validoot
        boolean hasErrors = false;
        for (int i = 0; i < studentParts.size(); i++) {
            var schemaPart = studentParts.get(i).getSchemaPart();
            courseSchemaInputDto.getParts().add(modelMapper.map(schemaPart, CourseSchemaPartDto.class));

            if (dataParts.size() == i) {
                dataParts.add(new CourseStudentPartDto(studentParts.get(i).getData()));
            }

            CourseStudentPartDto dataPart = dataParts.get(i);

            if (schemaPart.isLocked()) {
                // locked data isn't sent in post request
                dataPart.setData(studentParts.get(i).getData());
                continue;
            }
            if (!schemaPart.isValidator()) {
                continue;
            }
            if (!dataPart.getData().matches(schemaPart.getValidatorRegex())) {
                courseSchemaInputDto.getErrors().put(i, schemaPart.getValidatorMessage());
                hasErrors = true;
            }
        }

        model.addAttribute("updateLocked", studentParts.stream().allMatch(s -> s.getSchemaPart().isLocked()));

        if (hasErrors) {
            return "course/tab-input";
        }

        for (int i = 0; i < studentParts.size(); i++) {
            var studentPart = studentParts.get(i);
            if (!studentPart.getSchemaPart().isLocked()) {
                studentPart.setData(dataParts.get(i).getData());
            }
        }
        courseStudentService.saveStudentParts(studentParts);

        alertService.addAlertToResponse(response, "success", "Updated data");
        return "course/tab-input";
    }

    @GetMapping("/data")
    public String getDataTab(@PathVariable Course course, Model model) {
        var rows = course.getCourseStudents()
                .stream()
                .sorted(Comparator.comparingLong(CourseStudent::getId))
                .map(courseStudentPartRepository::findCourseStudentPartsByCourseStudentOrderBySchemaPart_Order)
                .toList();

        var headers = course.getSchemaParts()
                .stream()
                .sorted(Comparator.comparingInt(SchemaPart::getOrder))
                .map(SchemaPart::getName)
                .toList();

        model.addAttribute("headers", headers);
        model.addAttribute("rows", rows);

        return "course/tab-data";
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/students")
    public String getStudentsTab(@PathVariable Course course, Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        model.addAttribute("students", course.getCourseStudents().stream().map(CourseStudent::getAccount).toList());
        return "course/tab-students";
    }

    @Secured("ROLE_TEACHER")
    @DeleteMapping("/students/{studentId}/kick")
    public String kickFromCourse(@PathVariable Course course, @ModelAttribute Account account, @PathVariable int studentId, Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }

        var student = accountService.getAccountById(studentId);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        if (!courseService.kickFromCourse(course, student)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't kick user from the course");
        }

        var students = course.getCourseStudents()
                .stream()
                .map(CourseStudent::getAccount)
                .filter(a -> !Objects.equals(a.getId(), student.getId()))
                .toList();

        model.addAttribute("students", students);

        // remove isStudent when kicking self
        if (Objects.equals(account.getId(), student.getId())) {
            model.addAttribute("isStudent", false);
        }

        return "course/tab-students";
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/keys")
    public String getKeysTab(@SuppressWarnings("unused") @PathVariable Course course, Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        return "course/tab-keys";
    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/keys/create")
    public String createCourseKey(@PathVariable Course course, @RequestParam String key, Model model, HttpServletResponse response) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        if (courseService.addKey(course, key)) {
            alertService.addAlertToResponse(response, "success", "Added new key");
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Key has to be unique");
        }

        return "course/tab-keys";
    }

    @Secured("ROLE_TEACHER")
    @DeleteMapping("/keys/{keyId}/revoke")
    public String revokeCourseKey(@PathVariable Course course, @PathVariable int keyId, Model model, HttpServletResponse response) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        if (!courseService.deleteKey(course, keyId)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete the course");
        }

        alertService.addAlertToResponse(response, "success", "Revoked key");
        return "course/tab-keys";
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/settings")
    public String getSettingsTab(@SuppressWarnings("unused") @PathVariable Course course, Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized user");
        }

        return "course/tab-settings";
    }
}
