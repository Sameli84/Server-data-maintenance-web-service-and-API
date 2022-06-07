package com.example.servermaintenance.account;

import lombok.AllArgsConstructor;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.*;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Optional<Account> getContextAccount(Principal principal) {
        KeycloakAuthenticationToken keycloakAuthenticationToken = (KeycloakAuthenticationToken) principal;
        AccessToken accessToken = keycloakAuthenticationToken.getAccount().getKeycloakSecurityContext().getToken();
        var email = accessToken.getEmail();
        return accountRepository.findByEmail(email);
    }

    @Transactional
    public boolean registerAccount(AccessToken accessToken) {
        if (accountRepository.findByEmail(accessToken.getEmail()).isPresent()) {
            System.out.println("Was already in db");
            return false;
        }
        Account a = new Account(accessToken.getGivenName(), accessToken.getFamilyName(), accessToken.getEmail(), accessToken.getId());
        accountRepository.save(a);
        System.out.println("Saved to db");
        return true;
    }

    public List<Account> searchAccounts(String search) {
        return accountRepository.findAccountsByEmailContainingIgnoreCase(search);
    }

    public Account getAccountById(int accountId) {
        return accountRepository.getById((long) accountId);
    }

    @Transactional
    public void updateAccount(Account account) {
        accountRepository.save(account);
    }

    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

}
