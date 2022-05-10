package com.example.servermaintenance.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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

    public boolean registerStudent(String name, String email, String password) {
        return registerAccount(name, email, password, List.of(roleRepository.findByName("ROLE_STUDENT")));
    }

    @Transactional
    public boolean registerAccount(String name, String email, String password, List<Role> roles) {
        if (accountRepository.findByEmail(email).isPresent()) {
            return false;
        }

        Account a = new Account(name, email, passwordEncoder.encode(password));
        a.setRoles(roles);
        accountRepository.save(a);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByEmail(username).orElseThrow();
    }

    public List<Account> searchAccounts(String search) {
//        List<Account> accounts = accountRepository.findAll();
//        accounts.removeIf(p -> !p.getName().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)));
        var accounts = accountRepository.findAccountsByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search);
        return accounts;
    }

    public Account getAccountById(int accountId) {
        return accountRepository.getById((long)accountId);
    }

    @Transactional
    public void updateAccount(Account account) {
        accountRepository.save(account);
    }

    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    public void grantRights(int accountId, Role role) {
        var account = this.getAccountById(accountId);
        Collection<Role> roles = account.getRoles();

        role.getAccounts().add(account);
        roles.add(role);
        account.setRoles(roles);

        this.updateAccount(account);
        roleRepository.save(role);
    }

    public void removeRights(int accountId, Role role) {
        var account = this.getAccountById(accountId);
        Collection<Role> roles = account.getRoles();

        role.getAccounts().remove(account);
        roles.remove(role);
        account.setRoles(roles);

        this.updateAccount(account);
        roleRepository.save(role);
    }
}
