package com.user_management.security;

import com.user_management.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }

        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            }
        }

        if (!hasUpperCase) {
            throw new ValidationException("Password must contain at least one uppercase letter");
        }

        if (!hasLowerCase) {
            throw new ValidationException("Password must contain at least one lowercase letter");
        }

        if (!hasNumber) {
            throw new ValidationException("Password must contain at least one number");
        }
    }
}

