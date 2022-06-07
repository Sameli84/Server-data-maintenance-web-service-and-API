package com.example.servermaintenance.account;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AccountController {
    @GetMapping(path = "/logout")
    public String logout(HttpServletRequest request) throws ServletException {
        keycloakSessionLogout(request);
        tomcatSessionLogout(request);
        return "redirect:/";
    }
    private void keycloakSessionLogout(HttpServletRequest request){
        RefreshableKeycloakSecurityContext c = getKeycloakSecurityContext(request);
        KeycloakDeployment d = c.getDeployment();
        c.logout(d);
    }
    private void tomcatSessionLogout(HttpServletRequest request) throws ServletException {
        request.logout();
    }
    private RefreshableKeycloakSecurityContext getKeycloakSecurityContext(HttpServletRequest request){
        return (RefreshableKeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
    }
}
