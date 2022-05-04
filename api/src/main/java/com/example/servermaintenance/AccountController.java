package com.example.servermaintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String getRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String signUp(@RequestParam String name, @RequestParam String email, @RequestParam String password) {
        System.out.println("GOT");
        if (accountRepository.findByEmail(email).isPresent()) {
            // erroria tähän?
            System.out.println("ERRORIA");
            return "redirect:/register";
        }

        Account a = new Account(name, email, passwordEncoder.encode(password));
        accountRepository.save(a);
        return "redirect:/";
    }
}
