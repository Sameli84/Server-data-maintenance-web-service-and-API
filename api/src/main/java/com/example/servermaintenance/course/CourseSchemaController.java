package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.SchemaDto;
import com.example.servermaintenance.course.domain.SchemaPartDto;
import com.example.servermaintenance.interpreter.Interpreter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Objects;

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
    public Course addCourseToModel(@PathVariable Course course, @ModelAttribute Account account) {
        if (!Objects.equals(course.getOwner().getId(), account.getId()) && !roleService.isAdmin(account)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }
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

    @PostMapping("/cancel")
    public void cancelEditing(@PathVariable Course course, SessionStatus sessionStatus, HttpServletResponse response) {
        sessionStatus.setComplete();
        response.addHeader("HX-Redirect", "/courses/" + course.getUrl());
    }

    @GetMapping("/parts/add")
    public String addPartToSchema(@SuppressWarnings("unused") @PathVariable Course course,
                                  SchemaPartDto part,
                                  @ModelAttribute SchemaDto schemaDto) {
        schemaDto.setSelectedIndex(schemaDto.getParts().size());
        part.setName("");
        schemaDto.addPart(part);
        return "course/create-schema :: #schemaForm";
    }

    @DeleteMapping("/parts/{index}/delete")
    public String deletePartFromSchema(@SuppressWarnings("unused") @PathVariable Course course,
                                       @PathVariable int index,
                                       @ModelAttribute SchemaDto schemaDto) {
        var parts = schemaDto.getParts();
        if (parts.size() <= index + 1) {
            // is last, select new last index
            schemaDto.setSelectedIndex(parts.size() - 2);
        } // else don't change, next one under will be selected

        var partToRemove = parts.get(index);
        var schemaEntity = partToRemove.get_schemaPartEntity();
        if (schemaEntity != null) {
            schemaDto.markForRemoval(schemaEntity);
        }
        parts.remove(partToRemove);
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

    @PostMapping("/sort")
    public String sort(@SuppressWarnings("unused") @PathVariable Course course,
                       @RequestParam int drag,
                       @RequestParam int drop,
                       @ModelAttribute SchemaDto schemaDto) {
        // TODO: clamp drag and drop values
        // TODO: save drag, shift items from drop to drag and set drag to drop
        Collections.swap(schemaDto.getParts(), drag, drop);
        schemaDto.setSelectedIndex(drop);
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
