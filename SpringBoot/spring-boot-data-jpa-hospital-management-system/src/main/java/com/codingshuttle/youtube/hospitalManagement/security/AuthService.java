package com.codingshuttle.youtube.hospitalManagement.security;

import com.codingshuttle.youtube.hospitalManagement.dto.reponse.LoginResponseDto;
import com.codingshuttle.youtube.hospitalManagement.dto.reponse.RegisterResponseDto;
import com.codingshuttle.youtube.hospitalManagement.dto.request.LoginRequestDto;
import com.codingshuttle.youtube.hospitalManagement.dto.request.RegisterRequestDto;
import com.codingshuttle.youtube.hospitalManagement.entity.User;
import com.codingshuttle.youtube.hospitalManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);
        User user = (User) authentication.getPrincipal();
        String token = authUtil.generateAccessToken(user);
        return new LoginResponseDto(token, user.getId());
    }

    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        if (userRepository.findByUsername(registerRequestDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = userRepository.save(
                User.builder()
                        .username(registerRequestDto.getUsername().trim().toLowerCase())
                        .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                        .build()
        );

        return new RegisterResponseDto(user.getUsername(), user.getId());
    }
}