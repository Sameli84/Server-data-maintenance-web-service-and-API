package com.example.servermaintenance.course;

import com.example.servermaintenance.AlertService;
import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.CourseDataDto;
import com.example.servermaintenance.course.domain.DataGenerationDto;
import com.example.servermaintenance.interpreter.Interpreter;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/courses/{course}/data")
public class CourseDataController {
    private final CourseService courseService;
    private final AlertService alertService;

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

    @PostMapping("/cancel")
    public String cancelEdits(@SuppressWarnings("unused") @PathVariable Course course,
                              Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }
        return "course/tab-data";
    }

    @GetMapping("/edit")
    public String showEditView(@SuppressWarnings("unused") @PathVariable Course course,
                               @ModelAttribute CourseDataDto courseDataDto,
                               Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }

        return "course/tab-data-edit";
    }

    @GetMapping("/generate")
    public String showGenerateView(@SuppressWarnings("unused") @PathVariable Course course,
                                   @ModelAttribute CourseDataDto courseDataDto,
                                   Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }
        var slug = new Slugify().withUnderscoreSeparator(true);
        courseDataDto.setHeaders(courseDataDto.getHeaders().stream().map(slug::slugify).toList());
        model.addAttribute("dataGenerationDto", new DataGenerationDto());

        return "course/tab-data-generate";
    }

    @PostMapping("/generate")
    public String generate(@PathVariable Course course,
                           @ModelAttribute DataGenerationDto dataGenerationDto,
                           @ModelAttribute CourseDataDto courseDataDto,
                           HttpServletResponse response) {
        if (dataGenerationDto.getTarget() < 0 || dataGenerationDto.getTarget() >= courseDataDto.getHeaders().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        var slug = new Slugify().withUnderscoreSeparator(true);
        courseDataDto.setHeaders(courseDataDto.getHeaders().stream().map(slug::slugify).toList());

        int rowsAffected = 0;

        for (int i = 0; i < courseDataDto.getRows().size(); i++) {
            // selected rows will be shorter than rows if the last checkbox isn't checked
            if (dataGenerationDto.getSelectedRows() == null || i >= dataGenerationDto.getSelectedRows().size()) {
                break;
            } else if (dataGenerationDto.getSelectedRows().get(i) == null || !dataGenerationDto.getSelectedRows().get(i)) {
                continue;
            }

            var row = courseDataDto.getRows().get(i);

            var interpreter = new Interpreter(dataGenerationDto.getStatement())
                    .putLong("id", row.getIndex());

            var parts = row.getParts();
            for (int j = 0; j < parts.size(); j++) {
                interpreter.declareString(courseDataDto.getHeaders().get(j), parts.get(j).getData());
            }
            parts.get(dataGenerationDto.getTarget()).setData(interpreter.execute());

            if (interpreter.hasErrors()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Generator error: %s", interpreter.getErrors().get(0)));
            }

            rowsAffected++;
        }

        alertService.addAlertToResponse(response, "success", String.format("Rows affected %d", rowsAffected));

        courseService.saveCourseData(courseDataDto, course);
        return "course/tab-data-generate";
    }
}
