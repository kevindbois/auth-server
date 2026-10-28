package com.webshop.auth_server.controller;

import java.time.Duration;
import java.time.Instant;
import com.webshop.auth_server.dto.MeResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.webshop.auth_server.config.RsaKeyConfig;
import com.webshop.auth_server.dto.LoginRequest;
import com.webshop.auth_server.dto.RegisterRequest;
import com.webshop.auth_server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.webshop.auth_server.dto.LoginResponse;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * REST-endpoints för registrering, inloggning och information om inloggad användare.
 * Frontend anropar inte controllern direkt, utan alla anrop går via gatewayen.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RsaKeyConfig rsaKeyConfig;



    /**
     * Registrerar en ny användare med rollen USER.
     *
     * @param request användarnamn och lösenord
     * @return 200 OK med ett bekräftelsemeddelande
     */
    @PostMapping("/register")
    public ResponseEntity<String> register (@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }



    /**
     * Loggar in en användare och returnerar en signerad JWT.
     *
     * @param request användarnamn och lösenord
     * @return token, användarnamn, roller och tokenens livslängd i sekunder
     * @throws Exception om token inte kan signeras
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login (@RequestBody LoginRequest request) throws Exception {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }


    /**
     * Returnerar den inloggade användaren utifrån token.
     * Används av frontend när sidan laddas om, så att användaren förblir inloggad.
     * remainingSeconds talar om när frontend ska logga ut användaren automatiskt.
     *
     * @param jwt verifierad token, injiceras av Spring Security
     * @return användarnamn, roller och återstående sekunder
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal Jwt jwt) {
        Instant expiresAt = jwt.getExpiresAt();
        long remainingSeconds = expiresAt != null
                ? Math.max(0, Duration.between(Instant.now(), expiresAt).getSeconds())
                : 0;

        return ResponseEntity.ok(new MeResponse(
                jwt.getSubject(),
                jwt.getClaimAsStringList("roles"),
                remainingSeconds
        ));
    }



    /**
     * Publicerar den publika nyckeln i JWKS-format, så att andra tjänster
     * kan verifiera tokens utan tillgång till den privata nyckeln.
     *
     * @return en JWKS med en RSA-nyckel
     * @throws Exception om den publika nyckeln inte kan läsas
     */
    @GetMapping("/jwks")
    public ResponseEntity<Map<String, Object>> jwks() throws Exception {
        RSAPublicKey publicKey = rsaKeyConfig.buildPublicKey();

        Map<String, Object> jwk = new HashMap<>();
        jwk.put("alg", "RS256");
        jwk.put("kid", "auth-server-key");
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
