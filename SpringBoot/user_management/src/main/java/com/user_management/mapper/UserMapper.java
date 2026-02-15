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
        if (roles == null || roles.isEmpty()) {
            return Set.of("USER");
        }
        Set<String> roleNames = new HashSet<>();
        for (Role role : roles) {
            if (role != null && role.getRoleName() != null) {
                roleNames.add(role.getRoleName());
            }
        }
        return roleNames.isEmpty() ? Set.of("USER") : roleNames;
    }
}

