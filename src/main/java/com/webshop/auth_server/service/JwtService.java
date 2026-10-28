package com.webshop.auth_server.service;


import com.webshop.auth_server.config.RsaKeyConfig;
import com.webshop.auth_server.entity.User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;


/**
 * Skapar signerade JWT-tokens med RS256.
 * Bara auth-servern har den privata nyckeln och kan skapa tokens,
 * medan alla med den publika nyckeln kan verifiera dem.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    public static final long EXPIRATION_SECONDS = 60 * 60 * 24; // 24 timmar

    private final RsaKeyConfig rsaKeyConfig;



    /**
     * Skapar en signerad token som innehåller användarnamn (sub), roller,
     * utfärdandetid (iat) och utgångstid (exp).
     *
     * @param user användaren som token skapas för
     * @return token som en sträng
     * @throws Exception om den privata nyckeln inte kan läsas
     */
    public String generateToken(User user) throws Exception {
        return Jwts.builder()
                .header().add("kid", "auth-server-key").and()
                .subject(user.getUsername())
                .claim("roles", user.getRoles())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_SECONDS * 1000))
                .signWith(rsaKeyConfig.buildPrivateKey())
                .compact();
    }
}