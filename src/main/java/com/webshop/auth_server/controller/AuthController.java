package com.webshop.auth_server.controller;


import com.webshop.auth_server.config.RsaKeyConfig;
import com.webshop.auth_server.dto.LoginRequest;
import com.webshop.auth_server.dto.RegisterRequest;
import com.webshop.auth_server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RsaKeyConfig rsaKeyConfig;

    @PostMapping("/register")
    public ResponseEntity<String> register (@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login (@RequestBody LoginRequest request) throws Exception {
        String token = authService.login(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<String> registerAdmin(@RequestBody RegisterRequest request) {
        authService.createAdmin(request);
        return ResponseEntity.ok("Admin registered successfully");
    }



    @GetMapping("/jwks")
    public ResponseEntity<Map<String, Object>> jwks() throws Exception {
        RSAPublicKey publicKey = rsaKeyConfig.buildPublicKey();

        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("n", Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getModulus().toByteArray()));
        jwk.put("e", Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getPublicExponent().toByteArray()));

        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", List.of(jwk));

        return ResponseEntity.ok(jwks);
    }
}
