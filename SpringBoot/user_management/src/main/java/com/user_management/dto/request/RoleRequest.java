package com.user_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleRequest(
        @NotBlank(message = "Role name is required")
        @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
        String roleName,

        @Size(max = 255, message = "Role description must not exceed 255 characters")
        String roleDescription
) {}