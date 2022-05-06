package com.example.servermaintenance.account;

import com.example.servermaintenance.security.PasswordEncoderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    private boolean alreadySetup = false;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoderService passwordEncoderService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        var adminRole = createRoleIfNotFound("ADMIN");
        createRoleIfNotFound("TEACHER");
        createRoleIfNotFound("STUDENT");

        // TODO: lataa nämä jostain ympäristömuuttujista!
        createUserIfNotFound("admin@tuni.fi", "admini", "koira123", List.of(adminRole));

        alreadySetup = true;
    }

    @Transactional
    Role createRoleIfNotFound(String name) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
            roleRepository.save(role);
        }
        return role;
    }

    @Transactional
    void createUserIfNotFound(String email, String name, String password, List<Role> roles) {
        if (accountRepository.findByEmail(email).isPresent()) {
            return;
        }
        var account = new Account();
        account.setName(name);
        account.setEmail(email);
        account.setPassword(passwordEncoderService.passwordEncoder().encode(password));
        account.setRoles(roles);

        accountRepository.save(account);
    }
}
