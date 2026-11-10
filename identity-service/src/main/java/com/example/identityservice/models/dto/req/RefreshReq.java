package com.example.identityservice.models.dto.req;

import jakarta.validation.constraints.NotBlank;

public record RefreshReq(
        @NotBlank(message = "Refresh token không được để trống")
        String refreshToken
) {
}
