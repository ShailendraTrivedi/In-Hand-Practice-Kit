package com.user_management.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AssignRolesRequest(
        @NotEmpty(message = "At least one role is required")
        Set<String> roles
) {
}
