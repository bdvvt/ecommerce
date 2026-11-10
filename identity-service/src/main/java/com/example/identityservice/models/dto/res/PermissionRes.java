package com.example.identityservice.models.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRes {
    private Long id;
    private String name;
    private String displayName;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
