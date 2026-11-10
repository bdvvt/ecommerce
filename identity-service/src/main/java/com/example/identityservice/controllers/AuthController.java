package com.example.identityservice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.identityservice.models.dto.req.*;
import com.example.identityservice.models.dto.res.JwtRes;
import com.example.identityservice.models.dto.wrapper.ApiResponse;
import com.example.identityservice.models.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterReq req) {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đăng ký tài khoản thành công! Vui lòng kiểm tra email để nhận mã OTP kích hoạt.", null));
    }

    @PostMapping("/active-user")
    public ResponseEntity<ApiResponse<String>> activeUser(@Valid @RequestBody ActiveUserReq req) {
        String result = authService.activeUser(req);
        return ResponseEntity.ok(ApiResponse.success(result, null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtRes>> login(@Valid @RequestBody LoginReq req) {
        JwtRes jwtRes = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", jwtRes));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtRes>> refresh(@Valid @RequestBody RefreshReq req) {
        JwtRes jwtRes = authService.refresh(req);
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", jwtRes));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordReq req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok(ApiResponse.success("Nếu email tồn tại trên hệ thống, mã xác thực OTP đã được gửi đến email của bạn.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.", null));
    }
}
