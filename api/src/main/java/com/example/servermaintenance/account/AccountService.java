package com.example.servermaintenance.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService implements UserDetailsService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

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
        a.setRoles(List.of(roleRepository.findByName("STUDENT")));
        accountRepository.save(a);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByEmail(username).orElseThrow();
    }
}
