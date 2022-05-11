package com.example.servermaintenance.datarow;

import com.example.servermaintenance.account.AccountRepository;
import com.example.servermaintenance.account.AccountService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/bulkcreate")
    public String bulkcreate(Authentication authentication) {
// save a single Customer

        var account = accountRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isEmpty()) {
            return "redirect:/login";
        }

        System.out.println(account.toString());

        Course course = new Course("SoftaDevaus", "softa", account.get());
        courseRepository.save(course);
        dataRowRepository.save(new DataRow("Jakobi", "Juuseri", 55555, "theDNS", "myDNS", "Jaakko", "vpsJuuseri", "8.8.8.8", "123.123.124.12", account.get(), course));

    /*
        repository.saveAll(Arrays.asList(new DataRow("Salim", "Khan")
                , new DataRow("Rajesh", "Parihar")
                , new DataRow("Rahul", "Dravid")
                , new DataRow("Dharmendra", "Bhojwani")));

    */

        return "redirect:/datarowpage";
    }

    @Secured("ROLE_TEACHER")
    @GetMapping("/datarowpage")
    public String getDatarows(Model model, @RequestParam Optional<Long> selectCourse) {
        List<Course> courses = courseService.getCourses();
        model.addAttribute("courses", courses);
        List<DataRow> dataRows;
        if (selectCourse.isEmpty() || selectCourse.get() == 0) {
            dataRows = dataRowService.getDataRows();
        } else {
            Course course = courseService.getCourseById(selectCourse.get());
            dataRows = dataRowService.getCourseData(course);
        }
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

    @PostMapping("/datarowpage/{datarowId}/update")
    public String createData(@PathVariable Long datarowId,
                             @RequestParam String cscUsername,
                             @RequestParam String dnsName, @RequestParam String selfMadeDnsName,
                             @RequestParam String name, @RequestParam String vpsUserName,
                             @RequestParam String poutaDns, @RequestParam String ipAddress) {

        var data = dataRowService.getDataRowById(datarowId);

        if (data.isEmpty()) {
            return "redirect:/datarowpage" + "?error";
        } else {
            var account = accountService.getContextAccount().get();
            if (account != data.get().getCourse().getOwner()) {
                return "redirect:/datarowpage" + "?error";
            }
            data.get().update(data.get().getStudentAlias(), cscUsername, data.get().getUid(), dnsName, selfMadeDnsName, name, vpsUserName, poutaDns, ipAddress);
            courseService.updateStudentsData(data.get());
        }

        return "redirect:/datarowpage";
    }

}