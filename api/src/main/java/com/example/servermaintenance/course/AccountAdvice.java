package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@RequiredArgsConstructor
@ControllerAdvice(assignableTypes = {
        CourseController.class,
        CourseDataController.class,
        CoursesController.class,
        CourseSchemaController.class
})
public class AccountAdvice {
    @ExceptionHandler(AccountNotFoundException.class)
    public String processAccountException(HttpServletRequest request, HttpServletResponse response) {
        if (Objects.equals(request.getHeader("HX-Request"), "true")) {
            response.addHeader("HX-Redirect", "/login");
        }
        request.getSession().invalidate();
        return "redirect:/login";
    }

    @ModelAttribute("account")
    public Account addAccountToModel(@AuthenticationPrincipal UserDetails userDetails) {
        return (Account) userDetails;
    }
}
