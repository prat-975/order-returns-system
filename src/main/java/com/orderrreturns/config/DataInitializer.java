package com.orderrreturns.config;

import com.orderrreturns.entity.Role;
import com.orderrreturns.entity.User;
import com.orderrreturns.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
    }

    /**
     * Only the system administrator is pre-created so the app can be used on first run.
     * All customer accounts must sign up via /register.
     */
    private void seedAdmin() {
        if (!userRepository.existsByUsernameIgnoreCase("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }
    }
}
