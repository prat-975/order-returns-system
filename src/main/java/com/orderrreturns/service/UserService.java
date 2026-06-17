package com.orderrreturns.service;

import com.orderrreturns.dto.RegisterDto;
import com.orderrreturns.entity.Role;
import com.orderrreturns.entity.User;
import com.orderrreturns.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final String RESERVED_ADMIN_USERNAME = "admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(RegisterDto dto) {
        String username = normalizeUsername(dto.getUsername());

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsernameIgnoreCase(normalizeUsername(username));
    }

    public boolean isReservedUsername(String username) {
        return RESERVED_ADMIN_USERNAME.equalsIgnoreCase(normalizeUsername(username));
    }

    public String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
