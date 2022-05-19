package com.example.servermaintenance.datarow;

import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.course.Course;
import com.example.servermaintenance.course.CourseDataDTO;
import com.example.servermaintenance.course.CourseService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class DataRowController {
    private final DataRowService dataRowService;
    private final CourseService courseService;
    private final AccountService accountService;
    private final RoleService roleService;

    @Secured("ROLE_TEACHER")
    @GetMapping("/datarow")
    public String getDataRows(Model model, @RequestParam Optional<Long> course) {
        var account = accountService.getContextAccount().get();
        List<Course> courses;
        if (roleService.isAdmin(account)) {
            courses = courseService.getCourses();
        } else {
            courses = courseService.getCoursesByTeacher(account);
        }
        model.addAttribute("courses", courses);
        List<DataRow> dataRows;
        if (course.isEmpty() || course.get() == 0) {
            if (roleService.isAdmin(account)) {
                dataRows = dataRowService.getDataRows();
            } else {
                dataRows = dataRowService.getDataRowsByTeacher(account);
            }
        } else {
            if (roleService.isAdmin(account)) {
                dataRows = dataRowService.getCourseDataRows(courseService.getCourseById(course.get()));
            } else {
                dataRows = dataRowService.getDataRowsByCourseAndTeacher(account, courseService.getCourseById(course.get()));
            }
        }
        dataRows.sort(Comparator.comparing((final DataRow a) -> a.getCourse().getId()).thenComparing(DataRow::getId));
        model.addAttribute("dataRows", dataRows);
        return "datarow/page";

    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/datarow")
    public String filterDataRows(@RequestParam Optional<Long> course) {
        if (course.isEmpty() || course.get() == 0) {
            return "redirect:/datarow";
        }
        return "redirect:/datarow?course=" + course.get();
    }

    @Secured("ROLE_TEACHER")
    @PutMapping("/datarow/{datarowId}/edit")
    public String createData(@PathVariable Long datarowId, @ModelAttribute CourseDataDTO courseDataDTO, Model model, RedirectAttributes redirectAttributes) {
        var data = dataRowService.getDataRowById(datarowId);
        if (data == null) {
            redirectAttributes.addFlashAttribute("error", String.format("Datarow %d not found!", datarowId));
            return "redirect:/datarow";
        }
        var account = accountService.getContextAccount().get();
        if (account != data.getCourse().getOwner() && !roleService.isAdmin(account)) {
            model.addAttribute("dataRow", data);
            return "datarow/row";
        } else {
            model.addAttribute("dataRow", dataRowService.updateDataRow(data, courseDataDTO));
            return "datarow/row";
        }
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/datarow/{datarowId}")
    public String cancelData(@PathVariable Long datarowId, Model model) {
        model.addAttribute("dataRow", dataRowService.getDataRowById(datarowId));
        return "datarow/row";
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/datarow/{datarowId}/edit")
    public String getDatarow(@PathVariable Long datarowId, Model model) {
        model.addAttribute("dataRow", dataRowService.getDataRowById(datarowId));
        return "datarow/edit";
    }

}