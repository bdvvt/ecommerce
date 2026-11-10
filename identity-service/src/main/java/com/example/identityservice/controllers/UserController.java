package com.example.identityservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.identityservice.models.constants.RoleName;
import com.example.identityservice.models.constants.UserStatus;
import com.example.identityservice.models.dto.req.*;
import com.example.identityservice.models.dto.res.PageResponse;
import com.example.identityservice.models.dto.res.UserRes;
import com.example.identityservice.models.dto.wrapper.ApiResponse;
import com.example.identityservice.models.entities.User;
import com.example.identityservice.models.services.IUserService;
import com.example.identityservice.security.principal.MyUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/users", "/api/users"})
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    // ==============================================================================
    // 1. PROFILE & PASSWORD (Current User)
    // ==============================================================================

    @GetMapping({"/me", "/profile"})
    public ApiResponse<UserRes> getMyProfile(@AuthenticationPrincipal MyUserDetails customUserDetails) {
        String identifier = customUserDetails != null ? customUserDetails.getUsername() : null;
        return ApiResponse.success(
                "Lấy thông tin tài khoản thành công",
                userService.getProfile(identifier)
        );
    }

    @PutMapping({"/me", "/profile"})
    public ApiResponse<UserRes> updateProfile(
            @AuthenticationPrincipal MyUserDetails customUserDetails,
            @Valid @RequestBody UpdateProfileReq req
    ) {
        String identifier = customUserDetails != null ? customUserDetails.getUsername() : null;
        return ApiResponse.success(
                "Cập nhật thông tin tài khoản thành công",
                userService.updateProfile(identifier, req)
        );
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal MyUserDetails customUserDetails,
            @Valid @RequestBody ChangePasswordReq req
    ) {
        String identifier = customUserDetails != null ? customUserDetails.getUsername() : null;
        userService.changePassword(identifier, req);
        return ApiResponse.success("Đổi mật khẩu thành công", null);
    }

    @PutMapping("/change-password")
    public ApiResponse<Void> changePasswordPut(
            @AuthenticationPrincipal MyUserDetails customUserDetails,
            @Valid @RequestBody ChangePasswordReq req
    ) {
        String identifier = customUserDetails != null ? customUserDetails.getUsername() : null;
        userService.changePassword(identifier, req);
        return ApiResponse.success("Đổi mật khẩu thành công", null);
    }

    // ==============================================================================
    // 2. USER MANAGEMENT (Admin / Management)
    // ==============================================================================

    @PostMapping
    @PreAuthorize("hasAuthority('users.add') or hasRole('ROLE_ADMIN')")
    public ApiResponse<UserRes> addNewUser(
            @AuthenticationPrincipal MyUserDetails customUserDetails,
            @Valid @RequestBody UserReq req
    ) {
        User currentUser = customUserDetails != null ? customUserDetails.getUser() : null;
        return ApiResponse.created(
                "Tạo mới người dùng thành công",
                userService.createUser(currentUser, req)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('users.view') or hasRole('ROLE_ADMIN')")
    public ApiResponse<UserRes> findById(@PathVariable Long id) {
        return ApiResponse.success(
                "Lấy thông tin người dùng thành công",
                userService.findUserById(id)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users.edit') or hasRole('ROLE_ADMIN')")
    public ApiResponse<UserRes> updateUser(
            @AuthenticationPrincipal MyUserDetails customUserDetails,
            @PathVariable Long id,
            @Valid @RequestBody UserReq req
    ) {
        User currentUser = customUserDetails != null ? customUserDetails.getUser() : null;
        return ApiResponse.success(
                "Cập nhật người dùng thành công",
                userService.updateUser(currentUser, id, req)
        );
    }

    @PutMapping("/{id}/password")
    public ApiResponse<UserRes> updateUserPassword(
            @AuthenticationPrincipal MyUserDetails customUserDetails,
            @PathVariable Long id,
            @Valid @RequestBody UserPassReq req
    ) {
        User currentUser = customUserDetails.getUser();
        return ApiResponse.success(
                "Cập nhật mật khẩu người dùng thành công",
                userService.updateUserPassword(currentUser, id, req)
        );
    }

    @PutMapping("/{id}/role")
    public ApiResponse<UserRes> updateUserRole(
            @AuthenticationPrincipal MyUserDetails customUserDetails,
            @PathVariable Long id,
            @Valid @RequestBody UserRoleReq req
    ) {
        User currentUser = customUserDetails.getUser();
        return ApiResponse.success(
                "Cập nhật role người dùng thành công",
                userService.updateUserRole(currentUser, id, req)
        );
    }

    @PutMapping("/{id}/status")
    public ApiResponse<UserRes> updateUserStatus(
            @AuthenticationPrincipal MyUserDetails customUserDetails,
            @PathVariable Long id,
            @Valid @RequestBody UserStatusReq req
    ) {
        User currentUser = customUserDetails.getUser();
        return ApiResponse.success(
                "Cập nhật trạng thái người dùng thành công",
                userService.updateUserStatus(currentUser, id, req)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('users.delete') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> dropout(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('users.view') or hasRole('ROLE_ADMIN')")
    public ApiResponse<PageResponse<UserRes>> findAll(
            @PageableDefault(
                    page = 0,
                    size = 5,
                    sort = "userId",
                    direction = Sort.Direction.ASC
            ) Pageable pageable,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "role", required = false) RoleName role,
            @RequestParam(value = "status", required = false) UserStatus status
    ) {
        if (search != null && !search.trim().isEmpty()) {
            return ApiResponse.success(
                    "Tìm kiếm người dùng thành công",
                    userService.findAllBySearch(search, pageable)
            );
        }
        return ApiResponse.success(
                "Lấy danh sách người dùng thành công",
                userService.findAll(pageable, role, status)
        );
    }
}
