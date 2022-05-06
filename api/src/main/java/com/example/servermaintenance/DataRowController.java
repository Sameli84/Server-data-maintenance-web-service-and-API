package com.example.servermaintenance;

import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/datarowpage")
    public String getDatarows(Model model) {
        List<DataRow> dataRows = dataRowService.getDataRows();
        model.addAttribute("datarows", dataRows);
        return "datarowpage";
    }

    @PostMapping("/createdata")
    public String createData(@RequestParam String courseUrl, @RequestParam String studentAlias,
                             @RequestParam String cscUsername, @RequestParam int uid,
                             @RequestParam String dnsName, @RequestParam String selfMadeDnsName,
                             @RequestParam String name, @RequestParam String vpsUsername,
                             @RequestParam String poutaDns, @RequestParam String ipAddress, RedirectAttributes ra) {
        var course = courseRepository.findCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            // erroria?
            return "redirect:/";
        }
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var account = accountRepository.findByEmail(email);
        Boolean check = courseService.checkIfStudentOnCourse(course.get(), account.get());
        if(!check) {
            System.out.println("You must sign up for course to create projects!");
            ra.addFlashAttribute("error", "Not on course");
            return "redirect:/c/" + courseUrl;
        }


        dataRowRepository.save(new DataRow(studentAlias, cscUsername, uid, dnsName, selfMadeDnsName, name, vpsUsername, poutaDns, ipAddress, account.get(), course.get()));

        return "redirect:/c/" + courseUrl;
    }
}