package com.example.servermaintenance.account;

import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RolesAllowed("ADMIN")
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
    public String getRow(@PathVariable int accountId, Model model) {
        model.addAttribute("account", accountService.getAccountById(accountId));
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/row";
    }

    @GetMapping("/admin-tools/{accountId}/edit")
    public String getEditRoles(@PathVariable int accountId, Model model) {
        model.addAttribute("account", accountService.getAccountById(accountId));
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/roles";
    }

    @PutMapping("/admin-tools/{accountId}")
    public String updateRoles(@PathVariable int accountId, @RequestParam Optional<Boolean> student, @RequestParam Optional<Boolean> teacher,
                               @RequestParam Optional<Boolean> admin, Model model) {
        var account = accountService.getAccountById(accountId);
        model.addAttribute("account", account);
        model.addAttribute("roles", roleRepository.findAll());

        accountService.updateAccountRoles(account, student.isPresent(), teacher.isPresent(), admin.isPresent());

        return "admin/row";
    }
}
