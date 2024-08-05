package dev.nncode.gooutbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDto(@NotBlank String refreshToken) {

}
