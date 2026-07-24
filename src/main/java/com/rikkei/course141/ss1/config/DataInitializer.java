package com.rikkei.course141.ss1.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.rikkei.course141.ss1.model.User;
import com.rikkei.course141.ss1.repository.UserRepository;

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
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("123456"))
                .email("admin@test.com")
                .role("ADMIN")
                .enabled(true)
                .build());
            System.out.println("Seed data: admin/123456 (already activated)");
        }
    }
}
