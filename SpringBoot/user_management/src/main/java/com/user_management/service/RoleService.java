package com.user_management.service;

import com.user_management.entity.Role;
import com.user_management.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    role.setRoleDescription("Role: " + roleName);
                    return roleRepository.save(role);
                });
    }

    public Set<Role> getRolesByNames(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(getRoleByName(roleName));
        }
        return roles;
    }
}