package com.example.identityservice.models.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileReq {
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không vượt quá 100 ký tự")
    private String fullName;

    @Pattern(regexp = "^(03|05|07|08|09[2689])[0-9]{7}$", message = "Số điện thoại không đúng định dạng Việt Nam")
    private String phone;
}
