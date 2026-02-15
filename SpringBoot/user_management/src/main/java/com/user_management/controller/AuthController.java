package com.user_management.controller;

import com.user_management.dto.request.LoginRequest;
import com.user_management.dto.request.RegisterRequest;
import com.user_management.dto.response.LoginResponse;
import com.user_management.dto.response.UserResponse;
import com.user_management.mapper.UserMapper;
import com.user_management.security.util.JwtUtil;
import com.user_management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        UserResponse user = userMapper.toResponse(authService.loginAndGetEntity(loginRequest));
        List<String> roles = user.roles() != null && !user.roles().isEmpty() ? List.copyOf(user.roles()) : List.of("USER");
        String token = jwtUtil.generateToken(user.userId(), user.email(), roles);
        return ResponseEntity.ok(new LoginResponse(token, user));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUserProfile(Authentication authentication) {
        return ResponseEntity.ok(authService.getUserById(Long.parseLong(authentication.getName())));
    }
}