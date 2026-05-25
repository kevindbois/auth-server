package com.webshop.auth_server.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "rsa")
@Configuration
@Data
public class RsaKeyConfig {
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
}
