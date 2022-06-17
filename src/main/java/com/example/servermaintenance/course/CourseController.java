package com.example.servermaintenance.course;

import com.example.servermaintenance.AlertService;
import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.course.domain.*;
import com.github.slugify.Slugify;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

@Slf4j
@Controller
@AllArgsConstructor
@RequestMapping("/courses/{courseUrl}")
public class CourseController {
    private final AccountService accountService;
    private final CourseService courseService;
    private final CourseStudentService courseStudentService;
    private final ModelMapper modelMapper;
    private final AlertService alertService;
    private CourseRepository courseRepository;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean canEdit(Model model) {
        var canEdit = (Boolean) model.getAttribute("canEdit");
        if (canEdit == null) {
            return false;
        }
        return canEdit;
    }

    @GetMapping
    public String getCoursePage(@SuppressWarnings("unused") @PathVariable String courseUrl, @ModelAttribute Course course, @ModelAttribute Account account, Model model) {
        model.addAttribute("hasKey", courseService.hasCourseKey(course));

        if (courseService.isStudentOnCourse(course, account)) {
            var studentForm = courseService.getStudentForm(course, account);
            model.addAttribute("schemaInputDto", studentForm);
            model.addAttribute("updateLocked", studentForm.getParts().stream().allMatch(SchemaPartDto::isLocked));
        } else if (canEdit(model)) {
            model.addAttribute("courseDataDto", courseService.getCourseData(course));
        }

        return "course/page";
    }

    @PostMapping("/join")
    public String joinCourse(@SuppressWarnings("unused") @PathVariable String courseUrl, @ModelAttribute Course course, @RequestParam Optional<String> key, @ModelAttribute Account account, RedirectAttributes redirectAttributes) {
        if (courseService.joinToCourse(course, account, key.orElse(""))) {
            redirectAttributes.addFlashAttribute("success", "Joined course");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to join course " + course.getName());
        }
        return "redirect:/courses/" + course.getUrl();
    }

    @RolesAllowed("TEACHER")
    @DeleteMapping("/delete")
    public void deleteCourse(@SuppressWarnings("unused") @PathVariable String courseUrl, @ModelAttribute Course course, @ModelAttribute Account account, HttpServletResponse response, Model model) {
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
    public String getInputTab(@SuppressWarnings("unused") @PathVariable String courseUrl, @ModelAttribute Course course, @ModelAttribute Account account, Model model) {
        if (!courseService.isStudentOnCourse(course, account)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        var studentForm = courseService.getStudentForm(course, account);
        model.addAttribute("schemaInputDto", studentForm);
        model.addAttribute("updateLocked", studentForm.getParts().stream().allMatch(SchemaPartDto::isLocked));

        return "course/tab-input";
    }

    @PostMapping("/students/{studentId}/update")
    public String createData(@SuppressWarnings("unused") @PathVariable String courseUrl,
                             @ModelAttribute Course course,
                             @PathVariable Long studentId,
                             @ModelAttribute Account account,
                             @Valid @ModelAttribute SchemaInputDto schemaInputDto,
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

        schemaInputDto.setErrors(new HashMap<>());
        schemaInputDto.setParts(new ArrayList<>());

        var dataParts = schemaInputDto.getData();
        if (dataParts.size() != studentPartCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong amount of data parts");
        }

        // validoot
        boolean hasErrors = false;
        for (int i = 0; i < studentParts.size(); i++) {
            var schemaPart = studentParts.get(i).getSchemaPart();
            schemaInputDto.getParts().add(modelMapper.map(schemaPart, SchemaPartDto.class));

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
                schemaInputDto.getErrors().put(i, schemaPart.getValidatorMessage());
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

    @RolesAllowed("TEACHER")
    @GetMapping("/students")
    public String getStudentsTab(@SuppressWarnings("unused") @PathVariable String courseUrl, @ModelAttribute Course course, Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        model.addAttribute("students", courseRepository.findAllStudents(course));
        return "course/tab-students";
    }

    @RolesAllowed("TEACHER")
    @DeleteMapping("/students/{studentId}/kick")
    public String kickFromCourse(@SuppressWarnings("unused") @PathVariable String courseUrl, @ModelAttribute Course course, @ModelAttribute Account account, @PathVariable int studentId, Model model, HttpServletResponse response) {
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
            response.addHeader("HX-Redirect", "/courses/" + courseUrl);
        }

        return "course/tab-students";
    }

    @RolesAllowed("TEACHER")
    @GetMapping("/keys")
    public String getKeysTab(@SuppressWarnings("unused") @PathVariable String courseUrl, @ModelAttribute Course course, Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        return "course/tab-keys";
    }

    @RolesAllowed("TEACHER")
    @PostMapping("/keys/create")
    public String createCourseKey(@SuppressWarnings("unused") @PathVariable String courseUrl, @ModelAttribute Course course, @RequestParam String key, Model model, HttpServletResponse response) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        if (key.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Key can't be empty!");
        }
        if (courseService.addKey(course, key)) {
            alertService.addAlertToResponse(response, "success", "Added new key");
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Key has to be unique");
        }

        return "course/tab-keys";
    }

    @RolesAllowed("TEACHER")
    @DeleteMapping("/keys/{keyId}/revoke")
    public String revokeCourseKey(@SuppressWarnings("unused") @PathVariable String courseUrl, @ModelAttribute Course course, @PathVariable int keyId, Model model, HttpServletResponse response) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
        if (!courseService.deleteKey(course, keyId)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete the course");
        }

        alertService.addAlertToResponse(response, "success", "Revoked key");
        return "course/tab-keys";
    }

    @RolesAllowed("TEACHER")
    @GetMapping("/settings")
    public String getSettingsTab(@SuppressWarnings("unused") @PathVariable String courseUrl, @ModelAttribute Course course, Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized user");
        }

        return "course/tab-settings";
    }

    @RolesAllowed("TEACHER")
    @GetMapping("/course-name")
    public String getCourseName(@PathVariable String courseUrl, @ModelAttribute Course course) {
        return "course/course-name";
    }

    @RolesAllowed("TEACHER")
    @GetMapping("/name-cancel")
    public String getNameCancel(@PathVariable String courseUrl, @ModelAttribute Course course) {
        return "course/course-name-cancel";
    }

    @RolesAllowed("TEACHER")
    @PostMapping("/update-name")
    public String updateCourseName(@PathVariable String courseUrl, @ModelAttribute Course course, @ModelAttribute("changedName") String changedName) {
        System.out.println(changedName);
        course.setName(changedName);
        int slugNumber = Integer.parseInt(courseUrl.substring(courseUrl.length() - 1));
        course.setUrl(String.format("%s-%d", new Slugify().slugify(changedName), slugNumber));
        courseRepository.save(course);
        return "redirect:/courses/" + course.getUrl();
    }
}
