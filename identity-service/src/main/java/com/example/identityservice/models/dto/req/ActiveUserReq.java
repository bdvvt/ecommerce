package com.example.identityservice.models.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActiveUserReq {
    private String loginIdentifier;

    private String email;

    @NotBlank(message = "Mã OTP không được để trống")
    private String otp;

    public String getEffectiveIdentifier() {
        if (loginIdentifier != null && !loginIdentifier.isBlank()) return loginIdentifier;
        return email;
    }
}
