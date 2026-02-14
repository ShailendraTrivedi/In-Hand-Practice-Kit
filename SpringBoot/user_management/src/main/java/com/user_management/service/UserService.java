package com.user_management.service;

import com.user_management.dto.request.LoginRequest;
import com.user_management.dto.response.UserResponse;
import com.user_management.entity.Role;
import com.user_management.entity.User;
import com.user_management.mapper.UserMapper;
import com.user_management.repository.UserRepository;
import com.user_management.security.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final RoleService roleService;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse registerUser(com.user_management.dto.request.RegisterRequest request) {
        passwordService.validatePassword(request.password());

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("User with email " + request.email() + " already exists");
        }

        String hashedPassword = passwordService.hashPassword(request.password());

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(hashedPassword);
        user.setIsActive(true);

        Role userRole = roleService.getRoleByName("USER");
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public User loginAndGetEntity(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.email()));

        if (!passwordService.verifyPassword(loginRequest.password(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new RuntimeException("User account is inactive");
        }

        return user;
    }

    public UserResponse getUserById(Long id) {
        User user = getUserEntityById(id);
        return userMapper.toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserEntityById(id);
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse assignRolesToUser(Long userId, Set<String> roleNames) {
        User user = getUserEntityById(userId);
        Set<Role> roles = roleService.getRolesByNames(roleNames);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}