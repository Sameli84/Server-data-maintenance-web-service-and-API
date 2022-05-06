package com.example.servermaintenance.account;

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
    public String signUp(@Valid @ModelAttribute Account account, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return "register";
        }
        
        if (accountService.registerStudent(account.getName(), account.getEmail(), account.getPassword())) {
            return "redirect:/";
        } else {
            return "redirect:/register?error";
        }
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

}
