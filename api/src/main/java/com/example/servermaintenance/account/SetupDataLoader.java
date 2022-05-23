package com.example.servermaintenance.account;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Set;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    public SetupDataLoader(RoleService roleService, AccountService accountService, Environment env) {
        this.roleService = roleService;
        this.accountService = accountService;
        this.env = env;
    }

    private boolean alreadySetup = false;
    private final RoleService roleService;
    private final AccountService accountService;
    private final Environment env;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        var adminRole = createRoleIfNotFound("ADMIN");
        createRoleIfNotFound("TEACHER");
        createRoleIfNotFound("STUDENT");

        var root = new RegisterDTO();
        root.setFirstName(env.getProperty("spring.security.user.firstname"));
        root.setLastName(env.getProperty("spring.security.user.lastname"));
        root.setEmail(env.getProperty("spring.security.user.email"));
        root.setPassword(env.getProperty("spring.security.user.password"));

        accountService.registerAccount(root, Set.of(adminRole));

        alreadySetup = true;
    }

    @Transactional
    Role createRoleIfNotFound(String name) {
        Role role = roleService.getRole(name);
        if (role == null) {
            role = roleService.createRole(name);
        }
        return role;
    }
}
