package com.example.servermaintenance;

import com.example.servermaintenance.account.AccountNotFoundException;
import com.example.servermaintenance.account.AccountService;
import org.apache.http.client.HttpClient;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationSuccessHandler;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        if (authentication.getPrincipal() instanceof KeycloakPrincipal<?> principal) {
            AccessToken token = principal.getKeycloakSecurityContext().getToken();
            try {
                accountService.syncAccount(token);
            } catch (AccountNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}