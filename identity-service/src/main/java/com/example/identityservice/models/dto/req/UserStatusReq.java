package com.example.identityservice.models.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.identityservice.models.constants.UserStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusReq {
    @NotNull(message = "Trạng thái không được để trống")
    private UserStatus status;
}
