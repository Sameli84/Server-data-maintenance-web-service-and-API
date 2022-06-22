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
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/courses/{courseUrl}/data")
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
    @RolesAllowed("ROLE_STUDENT")
    public String getDataTab(@SuppressWarnings("unused") @PathVariable String courseUrl,
                             @ModelAttribute Course course,
                             @ModelAttribute CourseDataDto courseDataDto) {
        return "course/tab-data";
    }

    @PostMapping("/save")
    @Secured("ROLE_TEACHER")
    public String saveEdits(@SuppressWarnings("unused") @PathVariable String courseUrl,
                            @ModelAttribute Course course,
                            @ModelAttribute CourseDataDto courseDataDto,
                            Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }
        courseService.saveCourseData(courseDataDto, course);

        return "course/tab-data";
    }

    @PostMapping("/cancel")
    @Secured("ROLE_TEACHER")
    public String cancelEdits(@SuppressWarnings("unused") @PathVariable String courseUrl,
                              @ModelAttribute Course course,
                              Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }
        return "course/tab-data";
    }

    @GetMapping("/edit")
    @Secured("ROLE_TEACHER")
    public String showEditView(@SuppressWarnings("unused") @PathVariable String courseUrl,
                               @ModelAttribute Course course,
                               @ModelAttribute CourseDataDto courseDataDto,
                               Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }

        return "course/tab-data-edit";
    }

    @GetMapping("/generate")
    @Secured("ROLE_TEACHER")
    public String showGenerateView(@SuppressWarnings("unused") @PathVariable String courseUrl,
                                   @ModelAttribute Course course,
                                   @ModelAttribute CourseDataDto courseDataDto,
                                   Model model) {
        if (!canEdit(model)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }
        var slug = new Slugify().withUnderscoreSeparator(true);
        // Parses multiword data headers to strings with no spaces, so they can be used with generation tool
        courseDataDto.setHeaders(courseDataDto.getHeaders().stream().map(slug::slugify).toList());
        model.addAttribute("dataGenerationDto", new DataGenerationDto());

        return "course/tab-data-generate";
    }

    @PostMapping("/generate")
    @Secured("ROLE_TEACHER")
    public String generate(@SuppressWarnings("unused") @PathVariable String courseUrl,
                           @ModelAttribute Course course,
                           @ModelAttribute DataGenerationDto dataGenerationDto,
                           @ModelAttribute CourseDataDto courseDataDto,
                           HttpServletResponse response) {
        if (dataGenerationDto.getTarget() < 0 || dataGenerationDto.getTarget() >= courseDataDto.getHeaders().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        var slug = new Slugify().withUnderscoreSeparator(true);
        // Parses multiword data headers to strings with no spaces, so they can be used with interpreter tool
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

            // Create interpreter tool with given statement
            var interpreter = new Interpreter(dataGenerationDto.getStatement())
                    .putLong("id", row.getIndex());

            var parts = row.getParts();
            // Generate new data for selected rows with interpreter tool
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
