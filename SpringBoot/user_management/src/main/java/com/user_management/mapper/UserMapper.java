package com.user_management.mapper;

import com.user_management.dto.response.UserResponse;
import com.user_management.entity.Role;
import com.user_management.entity.User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getIsActive(),
                extractRoleNames(user.getRoles())
        );
    }

    private Set<String> extractRoleNames(Set<Role> roles) {
        Set<String> roleNames = new HashSet<>();
        if (roles != null && !roles.isEmpty()) {
            Set<Role> rolesCopy = new HashSet<>(roles);
            for (Role role : rolesCopy) {
                if (role != null && role.getRoleName() != null) {
                    roleNames.add(role.getRoleName());
                }
            }
        }
        if (roleNames.isEmpty()) {
            roleNames.add("USER");
        }
        return roleNames;
    }
}

