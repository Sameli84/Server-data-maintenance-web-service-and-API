package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.SchemaDto;
import com.example.servermaintenance.course.domain.SchemaPartDto;
import com.example.servermaintenance.interpreter.Interpreter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.NoSuchElementException;

@Slf4j
@Secured("ROLE_TEACHER")
@Controller
@SessionAttributes("schemaDto")
@AllArgsConstructor
@RequestMapping("/courses/{course}/schema")
public class CourseSchemaController {
    private final CourseService courseService;
    private final AccountService accountService;
    private final SchemaPartRepository schemaPartRepository;
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

    @ModelAttribute("course")
    public Course addCourseToModel(@PathVariable Course course) {
        return course;
    }

    @ModelAttribute("schemaDto")
    public SchemaDto schema(@ModelAttribute Course course) {
        var schemaParts = schemaPartRepository.findSchemaPartsByCourseOrderByOrder(course); // TODO: put this call behind service
        var schemaDto = new SchemaDto();
        if (schemaParts.isEmpty()) {
            schemaDto.addPart(new SchemaPartDto());
            return schemaDto;
        }

        for (var sp : schemaParts) {
            var spd = modelMapper.map(sp, SchemaPartDto.class);
            spd.set_schemaPartEntity(sp);
            schemaDto.addPart(spd);
        }
        return schemaDto;
    }

    @GetMapping
    public String showCourseSchemaPage(@SuppressWarnings("unused") @PathVariable Course course,
                                       @ModelAttribute SchemaDto schemaDto) {
        return "course/create-schema";
    }

    @PostMapping
    public String createCourseSchema(@PathVariable Course course,
                                     @ModelAttribute SchemaDto schemaDto,
                                     @ModelAttribute Account account,
                                     SessionStatus sessionStatus) {
        courseService.saveCourseSchema(course, schemaDto);
        sessionStatus.setComplete();
        return "redirect:/courses/" + course.getUrl();
    }


    @GetMapping("/parts/add")
    public String addPartToSchema(@SuppressWarnings("unused") @PathVariable Course course,
                                  SchemaPartDto part,
                                  @ModelAttribute SchemaDto schemaDto) {
        schemaDto.addPart(part);
        return "course/create-schema :: #schemaForm";
    }

    @DeleteMapping("/parts/{index}/delete")
    public String deletePartFromSchema(@SuppressWarnings("unused") @PathVariable Course course,
                                       @PathVariable int index,
                                       @ModelAttribute SchemaDto schemaDto) {
        var partToRemove = schemaDto.getParts().get(index);
        var schemaEntity = partToRemove.get_schemaPartEntity();
        if (schemaEntity != null) {
            schemaDto.markForRemoval(schemaEntity);
        }
        schemaDto.getParts().remove(partToRemove);
        return "course/create-schema :: #schemaForm";
    }

    @PostMapping("/parts/{index}/reset")
    public String resetPartToOriginalState(@SuppressWarnings("unused") @PathVariable Course course,
                                           @PathVariable int index,
                                           @ModelAttribute SchemaDto schemaDto) {
        var part = schemaDto.getParts().get(index);
        var schemaEntity = part.get_schemaPartEntity();
        if (schemaEntity != null) {
            var resetPart = modelMapper.map(schemaEntity, SchemaPartDto.class);
            resetPart.set_schemaPartEntity(schemaEntity);

            // Remember order
            resetPart.setOrder(index);

            schemaDto.getParts().set(index, resetPart);
        }

        return "course/create-schema :: #schemaForm";
    }

    @PostMapping("/render")
    public String renderSchema(@SuppressWarnings("unused") @PathVariable Course course,
                               @ModelAttribute SchemaDto schemaDto) {
        return "course/create-schema :: #render";
    }

    @PostMapping("/parts/{id}/generate")
    public String renderGenerationStatement(@SuppressWarnings("unused") @PathVariable Course course,
                                            @PathVariable int id,
                                            @ModelAttribute SchemaDto schemaDto,
                                            Model model) {
        int revolutions = 10;
        var out = new ArrayList<String>(revolutions);
        var interpreter = new Interpreter(schemaDto.getParts().get(id).getGenerationStatement());
        for (int i = 0; i < revolutions; i++) {
            out.add(interpreter.declareInt("id", i).execute());
        }

        model.addAttribute("out", out);
        return "course/create-schema :: #repl";
    }
}
