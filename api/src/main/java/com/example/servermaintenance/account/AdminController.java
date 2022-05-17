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
public class AdminController {
    private final AccountService accountService;
    private final RoleRepository roleRepository;

    @GetMapping("/admin-tools")
    public String getAdminPage(Model model, @ModelAttribute("searchName") Optional<String> searchString) {
        if (searchString.isPresent()) {
            model.addAttribute("searchName", searchString.get().split(",")[0]);
        }
        return "admin/tools";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/admin-tools/{accountId}/grant-remove")
    public String grantRights(RedirectAttributes ra, @PathVariable int accountId, @RequestParam Optional<String> selectRole, @RequestParam Optional<String> submit, @RequestParam Optional<String> searchName) {
        if (!accountService.getAccounts().contains(accountService.getAccountById(accountId))) {
            return "redirect:/admin-tools/" + "?error";
        }
        if (selectRole.isEmpty()) {
            return "redirect:/admin-tools/" + "?error";
        }

        searchName.ifPresent(s -> ra.addFlashAttribute("searchName", s));

        String roleString = "ROLE_" + selectRole.get().toUpperCase(Locale.ROOT);
        Role role = roleRepository.findByName(roleString);

        if (submit.isPresent()) {
            if (submit.get().equals("Grant")) {
                accountService.grantRights(accountId, role);
            }
            if (submit.get().equals("Remove")) {
                accountService.removeRights(accountId, role);
            }
        } else {
            return "redirect:/admin-tools/" + "?error";
        }

        return "redirect:/admin-tools";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/search")
    public String searchAccounts(Model model, @RequestParam Optional<String> search) {
        if (search.isPresent()) {
            List<Account> accounts = accountService.searchAccounts(search.get());
            model.addAttribute("accounts", accounts);
            List<String> roles = roleRepository.findAll().stream().map(Role::getName).map(n -> n.substring(n.indexOf("_") + 1).toLowerCase(Locale.ROOT)).toList();
            model.addAttribute("roleNames", roles);
            List<Role> roleList = roleRepository.findAll();
            model.addAttribute("roles", roleList);
            model.addAttribute("searchString", search.get());
        }

        return "admin/rows";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/admin-tools/{accountId}")
    public String getDatarow(@PathVariable int accountId, Model model, @RequestParam Optional<String> searchString) {
        Account account = accountService.getAccountById(accountId);
        model.addAttribute("account", account);
        List<Role> roleList = roleRepository.findAll();
        model.addAttribute("roles", roleList);
        if(searchString.isPresent()) {
            model.addAttribute("searchString", searchString.get());
        }
        return "admin/rights";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/admin-tools/{accountId}/update")
    public String updateRights(RedirectAttributes ra, @PathVariable int accountId, @RequestParam Optional<String> student, @RequestParam Optional<String> teacher,
                               @RequestParam Optional<String> admin, Model model) {
        model.addAttribute("account", accountService.getAccountById(accountId));
        model.addAttribute("roles", roleRepository.findAll());

        if (student.isPresent()) {
            accountService.grantRights(accountId, roleRepository.findByName("ROLE_STUDENT"));
        } else {
            accountService.removeRights(accountId, roleRepository.findByName("ROLE_STUDENT"));
        }

        if (teacher.isPresent()) {
            accountService.grantRights(accountId, roleRepository.findByName("ROLE_TEACHER"));
        } else {
            accountService.removeRights(accountId, roleRepository.findByName("ROLE_TEACHER"));
        }

        if (admin.isPresent()) {
            accountService.grantRights(accountId, roleRepository.findByName("ROLE_ADMIN"));
        } else {
            accountService.removeRights(accountId, roleRepository.findByName("ROLE_ADMIN"));
        }

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
