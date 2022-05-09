package com.example.servermaintenance.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    private boolean alreadySetup = false;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private Environment env;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        var adminRole = createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_TEACHER");
        createRoleIfNotFound("ROLE_STUDENT");

        var email = env.getProperty("spring.security.user.email");
        var name = env.getProperty("spring.security.user.name");
        var password = env.getProperty("spring.security.user.password");
        accountService.registerAccount(name, email, password, List.of(adminRole));

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
}
