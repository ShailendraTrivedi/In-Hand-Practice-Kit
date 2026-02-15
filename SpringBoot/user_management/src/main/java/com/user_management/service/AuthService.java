package com.user_management.service;

import com.user_management.dto.request.LoginRequest;
import com.user_management.dto.request.RegisterRequest;
import com.user_management.dto.response.UserResponse;
import com.user_management.entity.User;
import com.user_management.exception.DuplicateResourceException;
import com.user_management.exception.InvalidCredentialsException;
import com.user_management.exception.ResourceNotFoundException;
import com.user_management.mapper.UserMapper;
import com.user_management.repository.UserRepository;
import com.user_management.security.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final RoleService roleService;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        passwordService.validatePassword(request.password());
        String normalizedEmail = normalizeEmail(request.email());
        
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("User with email " + request.email() + " already exists");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordService.hashPassword(request.password()));
        user.setIsActive(true);
        user.getRoles().add(roleService.getRoleByName("USER"));

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public User loginAndGetEntity(LoginRequest loginRequest) {
        String normalizedEmail = normalizeEmail(loginRequest.email());
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordService.verifyPassword(loginRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new InvalidCredentialsException("User account is inactive");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    private String normalizeEmail(String email) {
        return email != null ? email.toLowerCase().trim() : null;
    }
}