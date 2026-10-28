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



/**
 * Affärslogik för registrering och inloggning.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;





    /**
     * Skapar en ny användare med rollen USER. Lösenordet hashas med BCrypt innan det sparas.
     *
     * @param request användarnamn och lösenord
     * @throws RuntimeException om användarnamnet redan finns
     */
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

    /**
     * Kontrollerar användarnamn och lösenord och skapar en JWT om de stämmer.
     *
     * @param request användarnamn och lösenord
     * @return token med användarnamn, roller och livslängd
     * @throws RuntimeException om användaren inte finns eller lösenordet är fel
     * @throws Exception om token inte kan signeras
     */
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

        return new LoginResponse(token, user.getUsername(), roles, jwtService.EXPIRATION_SECONDS);
    }
}
