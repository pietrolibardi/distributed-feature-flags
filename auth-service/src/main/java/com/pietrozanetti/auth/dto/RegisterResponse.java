package com.pietrozanetti.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String email,
        String role,
        LocalDateTime createdAt
) {}
