package com.example.identityservice.models.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterReq(
        String fullName,

        String username,

        String loginIdentifier,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
        String password,

        @Email(message = "Email không đúng định dạng")
        String email,

        String phone
) {
    public String getEffectiveIdentifier() {
        if (loginIdentifier != null && !loginIdentifier.isBlank()) return loginIdentifier;
        if (username != null && !username.isBlank()) return username;
        return email;
    }
}
