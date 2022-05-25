package com.example.servermaintenance.courseschema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Secured("ROLE_TEACHER")
@Controller
@SessionAttributes("courseSchemaDto")
public class CourseSchemaController {
    @ModelAttribute(name = "courseSchemaDto")
    public CourseSchemaDto schema() {
        var courseSchemaDto = new CourseSchemaDto();
        courseSchemaDto.addPart(new CourseSchemaPartDto());
        return courseSchemaDto;
    }

    @GetMapping("/schema")
    public String showCourseSchemaPage() {
        return "schema/create";
    }

    @PostMapping("/schema")
    public String createCourseSchema(@ModelAttribute CourseSchemaDto courseSchemaDto) {
        return "redirect:/courses";
    }

    @GetMapping("/schema/parts/add")
    public String addPartToSchema(CourseSchemaPartDto part, @ModelAttribute CourseSchemaDto courseSchemaDto) {
        courseSchemaDto.addPart(part);
        return "schema/create :: #schemaForm";
    }

    @DeleteMapping("/schema/parts/{index}/delete")
    public String deletePartFromSchema(@PathVariable int index, @ModelAttribute CourseSchemaDto courseSchemaDto) {
        courseSchemaDto.getParts().remove(index);
        return "schema/create :: #schemaForm";
    }

    @PostMapping("/schema/render")
    public String renderSchema(@ModelAttribute CourseSchemaDto courseSchemaDto) {
        return "schema/create :: #render";
    }
}
