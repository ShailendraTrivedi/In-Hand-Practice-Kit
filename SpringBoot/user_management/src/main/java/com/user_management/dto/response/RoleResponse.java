package com.user_management.dto.response;

public record RoleResponse(
        Long roleId,
        String roleName,
        String roleDescription,
        Integer userCount
) {}