package com.example.servermaintenance.account;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
public class AccountController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/register")
    public String getRegisterPage(@ModelAttribute RegisterDTO registerDTO) {
        return "register";
    }

    @PostMapping("/register")
    public String signUp(@Valid @ModelAttribute RegisterDTO registerDTO, BindingResult bindingResult, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // TODO: change to PRG
            return "register";
        }

        if (accountService.registerStudent(registerDTO)) {
            try {
                request.login(registerDTO.getEmail(), registerDTO.getPassword());
                return "redirect:/";
            } catch (ServletException e) {
                redirectAttributes.addFlashAttribute("success", "Please log in");
                return "redirect:/login";
            }
        } else {
            bindingResult.addError(new FieldError("registerDTO", "email", "Email already in use"));
            return "register";
        }
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/admin-tools")
    public String getAdminPage(Model model, @ModelAttribute("searchName") Optional<String> email) {
        email.ifPresent(s -> model.addAttribute("searchName", s));
        return "admin-tools";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/admin-tools/{accountId}/grant-remove")
    public String grantRights(RedirectAttributes ra, @PathVariable int accountId, @RequestParam Optional<String> selectRole, @RequestParam Optional<String> submit, @RequestParam Optional<String> searchName) {
        if(!accountService.getAccounts().contains(accountService.getAccountById(accountId))) {
            return "redirect:/admin-tools/" + "?error";
        }
        if(selectRole.isEmpty()) {
            return "redirect:/admin-tools/" + "?error";
        }

        searchName.ifPresent(s -> ra.addFlashAttribute("searchName", s));

        String roleString = "ROLE_" + selectRole.get().toUpperCase(Locale.ROOT);
        Role role = roleRepository.findByName(roleString);

        if(submit.isPresent()) {
            if(submit.get().equals("Grant")) {
                accountService.grantRights(accountId, role);
            }
            if(submit.get().equals("Remove")) {
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
        }

        return "account-table";
    }

}
