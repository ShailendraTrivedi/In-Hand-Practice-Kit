package com.user_management.dto.response;

import java.util.Set;

public record UserResponse(
        Long userId,
        String firstName,
        String lastName,
        String email,
        Boolean isActive,
        Set<String> roles
) {
}
