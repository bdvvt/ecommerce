package com.example.identityservice.controllers;

import com.example.identityservice.models.dto.res.PermissionRes;
import com.example.identityservice.models.dto.wrapper.ApiResponse;
import com.example.identityservice.models.services.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller tra cứu quyền hạn theo chuẩn pm-xuatban-backend.
 */
@RestController
@RequestMapping({"/api/v1/permissions", "/api/v1/admin/permissions"})
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Quản lý và tra cứu quyền hạn")
public class PermissionController {

    private final IPermissionService permissionService;

    @GetMapping("/by-feature")
    @Operation(
            summary = "Danh sách quyền theo chức năng",
            description = "Trả về danh sách các quyền của một chức năng mà user hiện tại có quyền truy cập (dựa trên Roles của user). Ví dụ: truyền name='users' sẽ trả về các quyền 'users.view', 'users.add',... mà user có trong roles của mình"
    )
    public ApiResponse<List<PermissionRes>> getPermissionsByFeatureName(
            @RequestParam(value = "name", required = true) String featureName
    ) {
        return ApiResponse.success(
                "Lấy danh sách quyền theo chức năng thành công",
                permissionService.getPermissionsByFeatureName(featureName)
        );
    }
}
