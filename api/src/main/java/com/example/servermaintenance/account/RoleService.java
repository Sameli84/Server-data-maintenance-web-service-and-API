package com.example.servermaintenance.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public boolean isAdmin(Account account) {
        var role = roleRepository.findByName("ROLE_ADMIN");
        return account.getAuthorities().contains(role);
    }

    public boolean isTeacher(Account account) {
        var role = roleRepository.findByName("ROLE_TEACHER");
        return account.getAuthorities().contains(role);
    }

    public boolean isStudent(Account account) {
        var role = roleRepository.findByName("ROLE_STUDENT");
        return account.getAuthorities().contains(role);
    }
}
