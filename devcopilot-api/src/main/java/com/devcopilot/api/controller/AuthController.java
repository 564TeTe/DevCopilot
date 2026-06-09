package com.devcopilot.api.controller;

import com.devcopilot.application.dto.AuthResult;
import com.devcopilot.application.service.AuthService;
import com.devcopilot.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResult> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request.username(), request.password(), request.displayName()));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResult> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request.username(), request.password()));
    }

    public record RegisterRequest(
            @NotBlank String username,
            @NotBlank String password,
            String displayName
    ) {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}
