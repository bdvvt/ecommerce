package com.example.identityservice.models.dto.req;

import jakarta.validation.constraints.NotBlank;

public record LoginReq(
        @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
        String username,

        @NotBlank(message = "Mật khẩu không được để trống")
        String password
) {
}
