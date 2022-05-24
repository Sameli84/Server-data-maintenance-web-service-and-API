package com.example.servermaintenance.CourseSchema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Secured("ROLE_TEACHER")
@Controller
@SessionAttributes("courseSchema")
public class CourseSchemaController {
    @ModelAttribute(name = "courseSchema")
    public CourseSchema schema() {
        var courseSchema = new CourseSchema();
        courseSchema.addPart(new CourseSchemaPart());
        return courseSchema;
    }

    @ModelAttribute("courseSchemaPart")
    public CourseSchemaPart courseSchemaPart() {
        return new CourseSchemaPart();
    }

    @GetMapping("/schema")
    public String showCourseSchemaPage() {
        return "schema/create";
    }

    @GetMapping("/schema/add")
    public String addPartToSchema(CourseSchemaPart part, @ModelAttribute CourseSchema courseSchema) {
        courseSchema.addPart(part);
        return "schema/create :: #schemaForm";
    }

    @DeleteMapping("/schema/part/{id}")
    public String deletePartFromSchema(@ModelAttribute CourseSchema courseSchema, @PathVariable int id) {
        courseSchema.getParts().remove(id);
        return "schema/create :: #schemaForm";
    }

    @PostMapping("/schema/render")
    public String renderSchema(@ModelAttribute CourseSchema courseSchema) {
        return "schema/render";
    }
}
