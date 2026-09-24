package com.webshop.auth_server.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String acessToken;
    private String subject;
    private List<String> roles;
}
