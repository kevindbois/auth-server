package com.webshop.auth_server.service;


import com.webshop.auth_server.config.RsaKeyConfig;
import com.webshop.auth_server.entity.User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final RsaKeyConfig rsaKeyConfig;

    public String generateToken(User user) throws Exception {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", user.getRoles())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(rsaKeyConfig.buildPrivateKey())
                .compact();
    }
}
