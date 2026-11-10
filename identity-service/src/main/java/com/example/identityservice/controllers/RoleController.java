package com.example.identityservice.controllers;

import com.example.identityservice.models.dto.req.AssignRolePermissionsReq;
import com.example.identityservice.models.dto.req.RoleReq;
import com.example.identityservice.models.dto.res.PermissionRes;
import com.example.identityservice.models.dto.res.RoleRes;
import com.example.identityservice.models.dto.wrapper.ApiResponse;
import com.example.identityservice.models.services.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý Role theo chuẩn kiến trúc pm-xuatban-backend.
 */
@RestController
@RequestMapping({"/api/v1/roles", "/api/v1/admin/roles"})
@RequiredArgsConstructor
@Tag(name = "Vai trò", description = "Quản lý danh mục vai trò (Role)")
public class RoleController {

    private final IRoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('roles.view')")
    @Operation(summary = "Danh sách vai trò", description = "Trả về danh sách tất cả vai trò đang hoạt động")
    public ApiResponse<List<RoleRes>> getRoles() {
        return ApiResponse.success(
                "Lấy danh sách vai trò thành công",
                roleService.getRoles()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('roles.view')")
    @Operation(summary = "Chi tiết vai trò", description = "Trả về thông tin chi tiết của một vai trò kèm danh sách quyền")
    public ApiResponse<RoleRes> getRoleById(@PathVariable Long id) {
        return ApiResponse.success(
                "Lấy thông tin vai trò thành công",
                roleService.getRoleById(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('roles.add')")
    @Operation(summary = "Tạo vai trò", description = "Tạo một vai trò mới")
    public ApiResponse<RoleRes> createRole(@Valid @RequestBody RoleReq request) {
        return ApiResponse.created(
                "Tạo vai trò thành công",
                roleService.createRole(request)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('roles.edit')")
    @Operation(summary = "Cập nhật vai trò", description = "Cập nhật thông tin của một vai trò")
    public ApiResponse<RoleRes> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleReq request
    ) {
        return ApiResponse.success(
                "Cập nhật vai trò thành công",
                roleService.updateRole(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('roles.delete')")
    @Operation(summary = "Xóa vai trò", description = "Xóa mềm vai trò (soft delete)")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success("Xóa vai trò thành công", null);
    }

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('roles.view') or hasAuthority('permissions.view')")
    @Operation(summary = "Chi tiết quyền của vai trò", description = "Trả về danh sách chi tiết các quyền của một vai trò")
    public ApiResponse<List<PermissionRes>> getPermissionsByRoleId(@PathVariable Long roleId) {
        return ApiResponse.success(
                "Lấy danh sách quyền của vai trò thành công",
                roleService.getPermissionsByRoleId(roleId)
        );
    }

    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('roles.edit') or hasAuthority('permissions.edit')")
    @Operation(summary = "Gán danh sách quyền cho vai trò", description = "Cập nhật danh sách các quyền gán cho một vai trò")
    public ApiResponse<RoleRes> assignPermissionsToRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignRolePermissionsReq request
    ) {
        return ApiResponse.success(
                "Gán quyền cho vai trò thành công",
                roleService.assignPermissionsToRole(roleId, request.getPermissionIds())
        );
    }
}
