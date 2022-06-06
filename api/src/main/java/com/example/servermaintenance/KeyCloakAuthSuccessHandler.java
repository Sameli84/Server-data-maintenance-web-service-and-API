package com.example.servermaintenance;

import com.example.servermaintenance.account.AccountRepository;
import com.example.servermaintenance.account.AccountService;
import lombok.AllArgsConstructor;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationSuccessHandler;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class KeyCloakAuthSuccessHandler extends KeycloakAuthenticationSuccessHandler {
    @Autowired
    private AccountService accountService;

    public KeyCloakAuthSuccessHandler(AuthenticationSuccessHandler fallback) {
        super(fallback);
    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication.getPrincipal() instanceof KeycloakPrincipal) {
            AccessToken token = ((KeycloakPrincipal<?>) authentication.getPrincipal()).getKeycloakSecurityContext().getToken();
            System.out.println("Jeabo22");

            accountService.registerAccount(token);
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
