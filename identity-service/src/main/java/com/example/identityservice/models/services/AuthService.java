package com.example.identityservice.models.services;

import jakarta.servlet.http.HttpServletRequest;
import com.example.identityservice.models.dto.req.*;
import com.example.identityservice.models.dto.res.JwtRes;

public interface AuthService {

    void register(RegisterReq req);

    JwtRes login(LoginReq req);

    JwtRes refresh(RefreshReq req);

    String activeUser(ActiveUserReq req);

    void logout(HttpServletRequest request);

    void forgotPassword(ForgotPasswordReq req);

    void resetPassword(ResetPasswordReq req);
}
