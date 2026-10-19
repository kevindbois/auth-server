package com.webshop.auth_server.dto;

import java.util.List;

public record MeResponse(String subject, List<String> roles) {}