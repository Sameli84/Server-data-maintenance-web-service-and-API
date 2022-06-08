package com.example.servermaintenance.course;

import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.CourseDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/courses/{course}/data")
public class CourseDataController {
    private final CourseService courseService;

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
