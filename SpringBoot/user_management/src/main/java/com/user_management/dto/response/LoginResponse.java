package com.user_management.dto.response;

public record LoginResponse(
        String token,
        UserResponse user
) {
}
