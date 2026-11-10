package com.example.identityservice.models.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Yêu cầu gán danh sách quyền cho vai trò")
public class AssignRolePermissionsReq {

    @NotNull(message = "Danh sách ID quyền không được null")
    @Schema(description = "Danh sách ID các Permission cần gán")
    private Set<Long> permissionIds;
}
