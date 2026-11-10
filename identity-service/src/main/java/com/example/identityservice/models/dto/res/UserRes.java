package com.example.identityservice.models.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.identityservice.models.constants.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRes {
    private Long userId;
    private String loginIdentifier;
    private UserStatus status;
    private Set<RoleRes> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
