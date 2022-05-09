package com.example.servermaintenance.account;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountController {
    @Autowired
    private AccountService accountService;

    @GetMapping("/register")
    public String getRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String signUp(@Valid @ModelAttribute Account account, BindingResult bindingResult, HttpServletRequest request) {
        if(bindingResult.hasErrors()){
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
    public String searchAccounts() {
        return "account-table";
    }

}
