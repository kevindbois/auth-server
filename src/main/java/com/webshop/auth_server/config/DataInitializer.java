package com.webshop.auth_server.config;

import com.webshop.auth_server.entity.Role;
import com.webshop.auth_server.entity.User;
import com.webshop.auth_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin@iths.se").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin@iths.se");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(new HashSet<>(Set.of(Role.ADMIN)));
            userRepository.save(admin);
        }
    }
}
