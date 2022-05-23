package com.example.servermaintenance.account;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public Optional<Account> getContextAccount() {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountRepository.findByEmail(email);
    }

    public boolean registerStudent(RegisterDTO registerDTO) {
        return registerAccount(registerDTO, Set.of(roleService.getRole("STUDENT")));
    }

    @Transactional
    public boolean registerAccount(RegisterDTO registerDTO, Set<Role> roles) {
        if (accountRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            return false;
        }

        Account a = new Account(registerDTO.getFirstName(), registerDTO.getLastName(), registerDTO.getEmail(), passwordEncoder.encode(registerDTO.getPassword()));
        a.setRoles(roles);
        accountRepository.save(a);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByEmail(username).orElseThrow();
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

    @Transactional
    public void addRole(Account account, Role role) {
        var roles = account.getRoles();

        role.getAccounts().add(account);
        roles.add(role);
        account.setRoles(roles);

        updateAccount(account);
        roleService.updateRole(role);
    }

    @Transactional
    public void removeRole(Account account, Role role) {
        var roles = account.getRoles();

        role.getAccounts().remove(account);
        roles.remove(role);
        account.setRoles(roles);

        updateAccount(account);
        roleService.updateRole(role);
    }

    @Transactional
    public void updateAccountRoles(Account account, boolean student, boolean teacher, boolean admin) {
        var roleFns = new HashMap<Boolean, Consumer<String>>() {{
            put(false, (String role) -> removeRole(account, roleService.getRole(role)));
            put(true, (String role) -> addRole(account, roleService.getRole(role)));
        }};
        roleFns.get(student).accept("STUDENT");
        roleFns.get(teacher).accept("TEACHER");
        roleFns.get(admin).accept("ADMIN");
    }
}
