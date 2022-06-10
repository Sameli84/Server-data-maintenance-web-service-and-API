package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
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

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Slf4j
@Secured("ROLE_TEACHER")
@Controller
@SessionAttributes("courseSchemaSessionMap")
@AllArgsConstructor
@RequestMapping("/courses/{course}/schema")
public class CourseSchemaController {
    private final CourseService courseService;
    private final SchemaPartRepository schemaPartRepository;
    private final ModelMapper modelMapper;

    private int clampToList(List<?> list, int index) {
        if (index < 0) {
            return 0;
        } else if (index >= list.size()) {
            return list.size() - 1;
        } else {
            return index;
        }
    }

    @ModelAttribute("courseSchemaSessionMap")
    public CourseSessionMap<SchemaDto> addCourseSessionMapToModel() {
        return new CourseSessionMap<>();
    }

    @ModelAttribute("schemaDto")
    public SchemaDto schema(@ModelAttribute Course course,
                            @ModelAttribute("courseSchemaSessionMap") CourseSessionMap<SchemaDto> courseSessionMap) {
        if (courseSessionMap.contains(course)) {
            return courseSessionMap.get(course);
        }
        var schemaParts = schemaPartRepository.findSchemaPartsByCourseOrderByOrder(course); // TODO: put this call behind service
        var schemaDto = courseSessionMap.getOrDefault(course, SchemaDto::new);
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
    public void createCourseSchema(@PathVariable Course course,
                                     @ModelAttribute SchemaDto schemaDto,
                                     @ModelAttribute Account account,
                                     @ModelAttribute("courseSchemaSessionMap") CourseSessionMap<SchemaDto> courseSessionMap,
                                     HttpServletResponse response) {
        courseService.saveCourseSchema(course, schemaDto);
        courseSessionMap.remove(course);
        response.addHeader("HX-Redirect", "/courses/" + course.getUrl());
    }

    @PostMapping("/cancel")
    public void cancelEditing(@PathVariable Course course,
                              @ModelAttribute("courseSchemaSessionMap") CourseSessionMap<SchemaDto> courseSessionMap,
                              HttpServletResponse response) {
        courseSessionMap.remove(course);
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
        index = clampToList(parts, index);
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
        var parts = schemaDto.getParts();
        index = clampToList(parts, index);
        var part = parts.get(index);
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

    @PostMapping("/removed-parts/recover")
    public String recoverPart(@PathVariable Course course,
                              @RequestParam int recover,
                              @ModelAttribute SchemaDto schemaDto) {
        var parts = schemaDto.getRemovedEntities().stream().filter(e -> e.getId() == recover).toList();
        if (parts.size() == 1) {
            var part = parts.get(0);
            schemaDto.getRemovedEntities().remove(part);

            var partDto = modelMapper.map(part, SchemaPartDto.class);
            partDto.set_schemaPartEntity(part);
            partDto.setOrder(schemaDto.getParts().size());

            schemaDto.addPart(partDto);
        }

        return "course/create-schema :: #schemaForm";
    }

    @PostMapping("/sort")
    public String sort(@SuppressWarnings("unused") @PathVariable Course course,
                       @RequestParam int drag,
                       @RequestParam int drop,
                       @ModelAttribute SchemaDto schemaDto) {
        var parts = schemaDto.getParts();
        drag = clampToList(parts, drag);
        drop = clampToList(parts, drop);
        var sub = parts.subList(Math.min(drag, drop), Math.max(drag, drop) + 1);
        if (drag < drop) {
            Collections.rotate(sub, -1);
        } else {
            Collections.rotate(sub, 1);
        }

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
            out.add(interpreter.putInt("id", i).execute());
        }

        model.addAttribute("out", out);
        return "course/create-schema :: #repl";
    }
}
