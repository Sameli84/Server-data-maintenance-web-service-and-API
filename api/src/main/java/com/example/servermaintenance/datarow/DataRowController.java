package com.example.servermaintenance.datarow;

import com.example.servermaintenance.account.AccountRepository;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.course.Course;
import com.example.servermaintenance.course.CourseRepository;
import com.example.servermaintenance.course.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
@Controller
public class DataRowController {

    @Autowired
    private DataRowService dataRowService;

    @Autowired
    private DataRowRepository dataRowRepository;

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseService courseService;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RoleService roleService;
    @Secured("ROLE_TEACHER")
    @GetMapping("/datarowpage")
    public String getDatarows(Model model, @RequestParam Optional<Long> selectCourse) {
        var account = accountService.getContextAccount().get();
        List<Course> courses;
        if (roleService.isAdmin(account)) {
            courses = courseService.getCourses();
        } else {
            courses = courseService.getCoursesByTeacher(account);
        }
        model.addAttribute("courses", courses);
        List<DataRow> dataRows;
        if (selectCourse.isEmpty() || selectCourse.get() == 0) {
            if (roleService.isAdmin(account)) {
                dataRows = dataRowService.getDataRows();
            } else {
                dataRows = dataRowService.getDataRowsByTeacher(account);
            }
        } else {
            Course course = courseService.getCourseById(selectCourse.get());
            if (roleService.isAdmin(account)) {
                dataRows = dataRowService.getCourseData(course);
            } else {
                dataRows = dataRowService.getDataRowsByCourseAndTeacher(account, course);
            }
        }
        dataRows.sort(Comparator.comparing((final DataRow a) -> a.getCourse().getId()).thenComparing(DataRow::getId));
        model.addAttribute("datarows", dataRows);
        return "datarowpage";

    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/datarowpage")
    public String filterDatarows(@RequestParam Optional<Long> selectCourse) {
        if (selectCourse.isEmpty() || selectCourse.get() == 0) {
            return "redirect:/datarowpage";
        }
        return "redirect:/datarowpage?selectCourse=" + selectCourse.get();
    }

    @Secured("ROLE_TEACHER")
    @PostMapping("/datarowpage/{datarowId}/update")
    public String createData(@PathVariable Long datarowId, @RequestParam Optional<Long> myParam,
                             @RequestParam String cscUsername,
                             @RequestParam String dnsName, @RequestParam String selfMadeDnsName,
                             @RequestParam String name, @RequestParam String vpsUserName,
                             @RequestParam String poutaDns, @RequestParam String ipAddress) {

        var data = dataRowService.getDataRowById(datarowId);

        Long selectCourse = myParam.get();

        if (data.isEmpty()) {
            return "redirect:/datarowpage" + "?error";
        } else {
            var account = accountService.getContextAccount().get();
            if ((account != data.get().getCourse().getOwner()) && (!roleService.isAdmin(account))) {
                return "redirect:/datarowpage" + "?error";
            }
            data.get().update(data.get().getStudentAlias(), cscUsername, data.get().getUid(), dnsName, selfMadeDnsName, name, vpsUserName, poutaDns, ipAddress);
            courseService.updateStudentsData(data.get());
        }

        if (selectCourse == 0) {
            return "redirect:/datarowpage";
        }
        return "redirect:/datarowpage?selectCourse=" + selectCourse;
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/datarowpage/{datarowId}")
    public String getDatarow(@PathVariable Long datarowId, Model model, @RequestParam Optional<Long> selectCourse) {
        System.out.println(selectCourse.get());
        DataRow dataRow = dataRowService.getDataRowById(datarowId).get();
        model.addAttribute("dataRow", dataRow);
        model.addAttribute("selectCourse", selectCourse.get());
        return "datarow";
    }

}