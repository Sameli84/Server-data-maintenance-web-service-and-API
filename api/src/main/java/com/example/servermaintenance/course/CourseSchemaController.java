package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.course.domain.SchemaDto;
import com.example.servermaintenance.course.domain.SchemaPartDto;
import com.example.servermaintenance.interpreter.Interpreter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CourseSchemaController {
    private final CourseService courseService;
    private final AccountService accountService;

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

    @ModelAttribute(name = "schemaDto")
    public SchemaDto schema() {
        var schemaDto = new SchemaDto();
        schemaDto.addPart(new SchemaPartDto());
        return schemaDto;
    }

    @GetMapping("/courses/schema")
    public String showCourseSchemaPage(@ModelAttribute SchemaDto schemaDto) {
        if (schemaDto.getCourseName() == null || schemaDto.getCourseName().isEmpty()) {
            return "redirect:/courses/create";
        }
        return "course/create-schema";
    }

    @PostMapping("/courses/schema")
    public String createCourseSchema(@ModelAttribute SchemaDto schemaDto, @ModelAttribute Account account, SessionStatus sessionStatus) {
        var course = courseService.createCourse(schemaDto, account);
        sessionStatus.setComplete();
        return "redirect:/courses/" + course.getUrl();
    }

    @GetMapping("/courses/create")
    public String showCourseCreationPage() {
        return "course/create-course";
    }

    @PostMapping("/courses/create")
    public String saveCourseCreationData(@ModelAttribute SchemaDto schemaDto) {
        return "redirect:/courses/schema";
    }

    @GetMapping("/courses/schema/parts/add")
    public String addPartToSchema(SchemaPartDto part, @ModelAttribute SchemaDto schemaDto) {
        schemaDto.addPart(part);
        return "course/create-schema :: #schemaForm";
    }

    @DeleteMapping("/courses/schema/parts/{index}/delete")
    public String deletePartFromSchema(@PathVariable int index, @ModelAttribute SchemaDto schemaDto) {
        schemaDto.getParts().remove(index);
        return "course/create-schema :: #schemaForm";
    }

    @PostMapping("/courses/schema/render")
    public String renderSchema(@ModelAttribute SchemaDto schemaDto) {
        return "course/create-schema :: #render";
    }

    @PostMapping("/courses/schema/statements/{id}/run")
    public String renderGenerationStatement(@PathVariable int id, @ModelAttribute SchemaDto schemaDto, Model model) {
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
