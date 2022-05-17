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
}
