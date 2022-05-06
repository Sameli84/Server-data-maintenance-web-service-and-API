package com.example.servermaintenance.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public Optional<Account> getContextAccount() {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountRepository.findByEmail(email);
    }

    public boolean registerAccount(String name, String email, String password) {
        if (accountRepository.findByEmail(email).isPresent()) {
            return false;
        }

        Account a = new Account(name, email, passwordEncoder.encode(password));
        accountRepository.save(a);
        return true;
    }
}
