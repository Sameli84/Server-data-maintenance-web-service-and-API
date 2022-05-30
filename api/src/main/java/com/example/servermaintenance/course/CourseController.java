package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.account.AccountService;
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
import java.util.stream.Collectors;

@Slf4j
@Controller
@AllArgsConstructor
public class CourseController {
    private final AccountService accountService;
    private final CourseService courseService;
    private final RoleService roleService;
    private final CourseKeyRepository courseKeyRepository;
    private final CourseStudentPartRepository courseStudentPartRepository;
    private final SchemaPartRepository schemaPartRepository;
    private final CourseStudentService courseStudentService;
    private final ModelMapper modelMapper;

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

    private boolean canEdit(Account account, Course course) {
        return Objects.equals(account.getId(), course.getOwner().getId()) || roleService.isAdmin(account);
    }

    @GetMapping("/")
    public String getIndexPage() {
        return "redirect:/courses";
    }

    @GetMapping("/courses")
    public String getCoursesPage(@ModelAttribute Account account, Model model) {
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

    @GetMapping("/courses/{course}")
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

        boolean isStudent = courseService.isStudentOnCourse(course, account);
        model.addAttribute("isStudent", isStudent);
        model.addAttribute("hasKey", courseService.hasCourseKey(course));
        boolean canEdit = canEdit(account, course);
        model.addAttribute("canEdit", canEdit);

        if (isStudent) {
            model.addAttribute("courseSchemaInputDto", courseService.getStudentForm(course, account));
        }

        return "course/page";
    }

    @GetMapping("/courses/{course}/input")
    public String getInputTab(@PathVariable Course course, @ModelAttribute Account account, Model model) {
        var isStudent = courseService.isStudentOnCourse(course, account);
        if (isStudent) {
            model.addAttribute("courseSchemaInputDto", courseService.getStudentForm(course, account));
        }
        model.addAttribute("isStudent", isStudent);
        model.addAttribute("canEdit", canEdit(account, course));

        return "course/tab-input";
    }

    @GetMapping("/courses/{course}/data")
    public String getDataTab(@PathVariable Course course, @ModelAttribute Account account, Model model) {
        model.addAttribute("canEdit", canEdit(account, course));
        model.addAttribute("isStudent", courseService.isStudentOnCourse(course, account));

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
    @GetMapping("/courses/{course}/students")
    public String getStudentsTab(@PathVariable Course course, @ModelAttribute Account account, Model model) {
        if (!canEdit(account, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        model.addAttribute("canEdit", true);
        model.addAttribute("isStudent", courseService.isStudentOnCourse(course, account));
        model.addAttribute("students", course.getCourseStudents().stream().map(CourseStudent::getAccount).toList());
        return "course/tab-students";
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/courses/{course}/keys")
    public String getKeysTab(@PathVariable Course course, @ModelAttribute Account account, Model model) {
        if (!canEdit(account, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        model.addAttribute("canEdit", true);
        model.addAttribute("isStudent", courseService.isStudentOnCourse(course, account));
        return "course/tab-keys";
    }

    @PostMapping("/courses/{course}/join")
    public String joinCourse(@PathVariable Course course, @RequestParam Optional<String> key, @ModelAttribute Account account, RedirectAttributes redirectAttributes) {
        if (courseService.joinToCourse(course, account, key.orElse(""))) {
            redirectAttributes.addFlashAttribute("success", "Joined course");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to join course " + course.getName());
        }
        return "redirect:/courses/" + course.getUrl();
    }

    @Secured("ROLE_TEACHER")
    @DeleteMapping("/courses/{course}/students/{studentId}/kick")
    public String kickFromCourse(@PathVariable Course course, @PathVariable int studentId, @ModelAttribute Account account, Model model) {
        if (!canEdit(account, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        model.addAttribute("canEdit", true);

        var student = accountService.getAccountById(studentId);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        if (!courseService.kickFromCourse(course, student)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't kick user from the course");
        }

        model.addAttribute("isStudent", courseService.isStudentOnCourse(course, account));
        return "course/tab-students";
    }

    @PostMapping("/courses/{course}/students/{studentId}/update-data")
    public String createData(@PathVariable Course course,
                             @PathVariable Long studentId,
                             @ModelAttribute Account account,
                             @Valid @ModelAttribute CourseSchemaInputDto courseSchemaInputDto,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (!Objects.equals(account.getId(), studentId) && !canEdit(account, course)) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized action");
            return "redirect:/courses/" + course;
        }

        if (!courseService.isStudentOnCourse(course, account)) {
            redirectAttributes.addFlashAttribute("error", "You must sign up for course before submitting data!");
            return "redirect:/courses/" + course;
        }

        var studentParts = courseStudentService.getCourseStudentParts(course, account);
        var schemaParts = schemaPartRepository.findSchemaPartsByCourseOrderByOrder(course);
        var dataParts = courseSchemaInputDto.getData();
        courseSchemaInputDto.setParts(schemaParts.stream().map(schemaPart -> modelMapper.map(schemaPart, CourseSchemaPartDto.class)).toList());
        courseSchemaInputDto.setErrors(new HashMap<>());

        if (schemaParts.size() != dataParts.size() || schemaParts.size() != studentParts.size()) {
            redirectAttributes.addFlashAttribute("error", "Wrong amount of data parts");
            return "redirect:/courses/" + course;
        }

        // validoot
        boolean hasErrors = false;
        for (int i = 0; i < schemaParts.size(); i++) {
            var schemaPart = schemaParts.get(i);
            if (!schemaPart.isValidator() || schemaPart.isLocked()) {
                continue;
            }
            var dataPart = dataParts.get(i);
            if (!dataPart.getData().matches(schemaPart.getValidatorRegex())) {
                courseSchemaInputDto.getErrors().put(i, schemaPart.getValidatorMessage());
                hasErrors = true;
            }
        }

        model.addAttribute("isStudent", courseService.isStudentOnCourse(course, account));
        model.addAttribute("canEdit", canEdit(account, course));

        if (hasErrors) {
            return "course/tab-input";
        }

        for (int i = 0; i < studentParts.size(); i++) {
            var studentPart = studentParts.get(i);
            if (!studentPart.getSchemaPart().isLocked()) {
                studentPart.setData(dataParts.get(i).getData());
            } else {
                dataParts.get(i).setData(studentPart.getData());
            }
        }
        courseStudentService.saveStudentParts(studentParts);

        return "course/tab-input";
    }


    @Secured("ROLE_TEACHER")
    @PostMapping("/courses/{course}/keys/create")
    public String createCourseKey(@PathVariable Course course, @RequestParam String key, @ModelAttribute Account account, Model model) {
        if (!canEdit(account, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        if (courseService.addKey(course, key)) {
            // TODO: add success
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete the course");
        }
        model.addAttribute("canEdit", true);
        model.addAttribute("isStudent", courseService.isStudentOnCourse(course, account));
        return "course/tab-keys";
    }

    @Secured("ROLE_TEACHER")
    @DeleteMapping("/courses/{course}/keys/{keyId}/revoke")
    public String revokeCourseKey(@PathVariable Course course, @PathVariable int keyId, @ModelAttribute Account account, Model model) {
        if (!canEdit(account, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        if (!courseService.deleteKey(course, keyId)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete the course");
        }
        model.addAttribute("canEdit", true);
        model.addAttribute("isStudent", courseService.isStudentOnCourse(course, account));
        return "course/tab-keys";
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/courses/{course}/settings")
    public String getSettingsTab(@PathVariable Course course, @ModelAttribute Account account, Model model) {
        if (!canEdit(account, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized user");
        }
        model.addAttribute("canEdit", true);
        model.addAttribute("isStudent", courseService.isStudentOnCourse(course, account));
        return "course/tab-settings";
    }

    @Secured("ROLE_TEACHER")
    @DeleteMapping("/courses/{course}/delete")
    public void deleteCourse(@PathVariable Course course, @ModelAttribute Account account, HttpServletResponse response) {
        if (!canEdit(account, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        if (!courseService.deleteCourse(course, account)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete the course");
        } else {
            response.addHeader("HX-Redirect", "/courses/");
        }
    }
}
