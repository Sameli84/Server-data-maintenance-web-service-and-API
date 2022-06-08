package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@ControllerAdvice(assignableTypes = {
        CourseController.class,
        CourseDataController.class,
        CoursesController.class,
        CourseSchemaController.class
})
public class AccountAdvice {
    private final AccountService accountService;

    @ExceptionHandler(AccountNotFoundException.class)
    public String processAccountException(HttpServletRequest request, HttpServletResponse response) {
        if (request.getHeader("HX-Request") != null) {
            response.addHeader("HX-Redirect", "/login");
        }
        request.getSession().invalidate();
        return "redirect:/login";
    }

    @ModelAttribute("account")
    public Account addAccountToModel() throws AccountNotFoundException {
        return accountService.getContextAccount().orElseThrow(AccountNotFoundException::new);
    }
}
