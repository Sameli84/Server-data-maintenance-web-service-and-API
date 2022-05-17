package com.example.servermaintenance.account;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public boolean isAdmin(Account account) {
        var role = getRole("ADMIN");
        return account.getAuthorities().contains(role);
    }

    public boolean isTeacher(Account account) {
        var role = getRole("TEACHER");
        return account.getAuthorities().contains(role);
    }

    public boolean isStudent(Account account) {
        var role = getRole("STUDENT");
        return account.getAuthorities().contains(role);
    }

    public Role getRole(String roleName) {
        return roleRepository.findByName("ROLE_" + roleName);
    }

    public Role createRole(String roleName){
        return roleRepository.save(new Role("ROLE_" + roleName));
    }

    public Role updateRole(Role role) {
        return roleRepository.save(role);
    }
}
