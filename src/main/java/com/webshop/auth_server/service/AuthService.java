package com.webshop.auth_server.service;

import com.webshop.auth_server.dto.LoginRequest;
import com.webshop.auth_server.dto.RegisterRequest;
import com.webshop.auth_server.entity.Role;
import com.webshop.auth_server.entity.User;
import com.webshop.auth_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import com.webshop.auth_server.dto.LoginResponse;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;




    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(new HashSet<>(Set.of(Role.USER)));

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) throws Exception {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        String token = jwtService.generateToken(user);
        List<String> roles = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toList());

        return new LoginResponse(token, user.getUsername(), roles);
    }
}
