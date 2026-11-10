package com.example.identityservice.models.dto.req;

import com.example.identityservice.models.constants.RoleName;
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
@Schema(description = "Yêu cầu tạo / cập nhật Role")
public class RoleReq {

    @NotNull(message = "Tên vai trò không được để trống")
    @Schema(description = "Tên vai trò", example = "ROLE_CATALOG_STAFF")
    private RoleName roleName;

    @Schema(description = "Mô tả vai trò", example = "Nhân viên quản lý danh mục sản phẩm")
    private String description;

    @Schema(description = "Mã quyền đặc thù", example = "ROLE_CATALOG_STAFF")
    private String permissionCode;

    @Schema(description = "Danh sách ID các quyền gán cho vai trò")
    private Set<Long> permissionIds;
}
