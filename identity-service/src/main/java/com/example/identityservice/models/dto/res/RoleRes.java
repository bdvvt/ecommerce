package com.example.identityservice.models.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Thông tin phản hồi của Role")
public class RoleRes {

    @Schema(description = "ID của Role", example = "1")
    private Long roleId;

    @Schema(description = "Tên vai trò", example = "ROLE_ADMIN")
    private String roleName;

    @Schema(description = "Mô tả vai trò", example = "Quản trị viên toàn quyền")
    private String description;

    @Schema(description = "Mã quyền đặc thù", example = "ROLE_ADMIN")
    private String permissionCode;

    @Schema(description = "Ngày tạo")
    private LocalDate createdAt;

    @Schema(description = "Ngày cập nhật")
    private LocalDate updatedAt;

    @Schema(description = "Danh sách quyền chi tiết của vai trò")
    private List<PermissionRes> permissions;
}
