package com.user_management.service;

import com.user_management.dto.request.RoleRequest;
import com.user_management.dto.response.RoleResponse;
import com.user_management.entity.Role;
import com.user_management.exception.DuplicateResourceException;
import com.user_management.exception.ResourceNotFoundException;
import com.user_management.exception.ValidationException;
import com.user_management.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        String roleName = request.roleName().toUpperCase();
        if (roleRepository.existsByRoleName(roleName)) {
            throw new DuplicateResourceException("Role with name " + request.roleName() + " already exists");
        }

        Role role = new Role();
        role.setRoleName(roleName);
        role.setRoleDescription(request.roleDescription() != null ? request.roleDescription() : "Role: " + roleName);
        return toResponse(roleRepository.save(role));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return toResponse(role);
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        String roleName = request.roleName().toUpperCase();

        if (!role.getRoleName().equals(roleName) && roleRepository.existsByRoleName(roleName)) {
            throw new DuplicateResourceException("Role with name " + request.roleName() + " already exists");
        }

        role.setRoleName(roleName);
        role.setRoleDescription(request.roleDescription() != null ? request.roleDescription() : "Role: " + roleName);
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            throw new ValidationException("Cannot delete role. " + role.getUsers().size()
                    + " user(s) are assigned to this role. Please remove the role from all users first.");
        }
        roleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));
    }

    @Transactional(readOnly = true)
    public Set<Role> getRolesByNames(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(getRoleByName(roleName));
        }
        return roles;
    }

    private RoleResponse toResponse(Role role) {
        int userCount = role.getUsers() != null ? role.getUsers().size() : 0;
        return new RoleResponse(role.getRoleId(), role.getRoleName(), role.getRoleDescription(), userCount);
    }
}