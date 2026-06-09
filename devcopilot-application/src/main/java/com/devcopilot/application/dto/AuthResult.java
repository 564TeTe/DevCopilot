package com.devcopilot.application.dto;

public record AuthResult(Long userId, String username, String displayName, String token) {
}
