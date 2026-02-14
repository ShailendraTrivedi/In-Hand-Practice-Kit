package com.user_management.controller;

import com.user_management.dto.request.AssignRolesRequest;
import com.user_management.dto.request.LoginRequest;
import com.user_management.dto.request.RegisterRequest;
import com.user_management.dto.response.LoginResponse;
import com.user_management.dto.response.UserResponse;
import com.user_management.entity.User;
import com.user_management.mapper.UserMapper;
import com.user_management.security.util.JwtUtil;
import com.user_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        UserResponse registeredUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        User userEntity = userService.loginAndGetEntity(loginRequest);
        UserResponse user = userMapper.toResponse(userEntity);

        List<String> roles = user.roles() != null && !user.roles().isEmpty() 
                ? List.copyOf(user.roles())
                : List.of("USER");
        
        String token = jwtUtil.generateToken(user.userId(), user.email(), roles);

        LoginResponse response = new LoginResponse(token, user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUserProfile(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> assignRolesToUser(
            @PathVariable Long id,
            @Valid @RequestBody AssignRolesRequest request) {
        UserResponse updatedUser = userService.assignRolesToUser(id, request.roles());
        return ResponseEntity.ok(updatedUser);
    }
}