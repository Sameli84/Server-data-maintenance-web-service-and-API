package com.example.servermaintenance.account;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String getRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String signUp(@Valid @ModelAttribute Account account, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (accountService.registerStudent(account.getName(), account.getEmail(), account.getPassword())) {
            try {
                request.login(account.getEmail(), account.getPassword());
                return "redirect:/";
            } catch (ServletException e) {
                return "redirect:/login?error";
            }
        } else {
            return "redirect:/register?error";
        }
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/admin-tools")
    public String getAdminPage() {
        return "admin-tools";
    }

    @PostMapping("/search")
    public String searchAccounts(Model model, @RequestParam Optional<String> search) {
        System.out.println(search.get());
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
