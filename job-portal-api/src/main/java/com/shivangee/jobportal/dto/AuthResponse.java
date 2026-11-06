package com.shivangee.jobportal.dto;

import com.shivangee.jobportal.entity.Role;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        Role role) {
}
