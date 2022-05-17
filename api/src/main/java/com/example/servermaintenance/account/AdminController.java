package com.example.servermaintenance.account;

import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
@AllArgsConstructor
@Secured("ROLE_ADMIN")
public class AdminController {
    private final AccountService accountService;
    private final RoleRepository roleRepository;

    @GetMapping("/admin-tools")
    public String getAdminPage(Model model, @ModelAttribute("searchName") Optional<String> searchString) {
        searchString.ifPresent(s -> model.addAttribute("searchName", s.split(",")[0]));
        return "admin/tools";
    }

    @PostMapping("/search")
    public String searchAccounts(Model model, @RequestParam Optional<String> search) {
        if (search.isPresent()) {
            model.addAttribute("accounts", accountService.searchAccounts(search.get()));
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("searchString", search.get());
        }

        return "admin/rows";
    }

    @GetMapping("/admin-tools/{accountId}")
    public String getDatarow(@PathVariable int accountId, Model model, @RequestParam Optional<String> searchString) {
        model.addAttribute("account", accountService.getAccountById(accountId));
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/rights";
    }

    @PostMapping("/admin-tools/{accountId}/update")
    public String updateRights(@PathVariable int accountId, @RequestParam Optional<Boolean> student, @RequestParam Optional<Boolean> teacher,
                               @RequestParam Optional<Boolean> admin, Model model) {
        var account = accountService.getAccountById(accountId);
        model.addAttribute("account", account);
        model.addAttribute("roles", roleRepository.findAll());

        accountService.updateAccountRoles(account, student.isPresent(), teacher.isPresent(), admin.isPresent());

        return "admin/row";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/admin-tools/{accountId}/update")
    public String cancelUpdate(RedirectAttributes ra, @PathVariable int accountId, @RequestParam Optional<String> student, @RequestParam Optional<String> teacher,
                               @RequestParam Optional<String> admin, Model model) {
        model.addAttribute("account", accountService.getAccountById(accountId));
        model.addAttribute("roles", roleRepository.findAll());

        return "admin/row";
    }
}
