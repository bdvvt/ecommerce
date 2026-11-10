package com.example.identityservice.models.services.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import com.example.identityservice.exceptions.AuthException;
import com.example.identityservice.exceptions.BadRequestException;
import com.example.identityservice.exceptions.NotFoundException;
import com.example.identityservice.models.constants.RoleName;
import com.example.identityservice.models.constants.UserStatus;
import com.example.identityservice.models.dto.req.ActiveUserReq;
import com.example.identityservice.models.dto.req.LoginReq;
import com.example.identityservice.models.dto.req.RefreshReq;
import com.example.identityservice.models.dto.req.RegisterReq;
import com.example.identityservice.models.dto.res.JwtRes;
import com.example.identityservice.models.entities.RefreshToken;
import com.example.identityservice.models.entities.Role;
import com.example.identityservice.models.entities.User;
import com.example.identityservice.models.repositories.RefreshTokenRepository;
import com.example.identityservice.models.repositories.RoleRepository;
import com.example.identityservice.models.repositories.UserRepository;
import com.example.identityservice.models.services.AuthService;
import com.example.identityservice.security.jwt.JwtUtils;
import com.example.identityservice.security.jwt.TokenBlacklistService;
import com.example.identityservice.security.principal.MyUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager manager;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserEventPublisher userEventPublisher;

    @Override
    @Transactional
    public void register(RegisterReq req) {
        String identifier = req.getEffectiveIdentifier();
        if (identifier == null || identifier.isBlank()) {
            throw new BadRequestException("Định danh đăng nhập (username hoặc email) không được để trống");
        }

        if (userRepository.existsByLoginIdentifier(identifier)) {
            throw new BadRequestException("Định danh đăng nhập đã được sử dụng");
        }

        Role userRole = roleRepository.findByRoleName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_USER)));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        // Sinh mã OTP 6 chữ số
        String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
        LocalDateTime otpExpiration = LocalDateTime.now().plusMinutes(5);

        String email = req.email();
        if (email == null || email.isBlank()) {
            if (identifier.contains("@")) {
                email = identifier;
            }
        }

        User user = User.builder()
                .loginIdentifier(identifier)
                .email(email)
                .fullName(req.fullName())
                .passwordHash(encoder.encode(req.password()))
                .roles(roles)
                .status(UserStatus.INACTIVE)
                .otpCode(otp)
                .otpExpiration(otpExpiration)
                .build();

        userRepository.save(user);

        // Tự động phát sự kiện đăng ký & gửi OTP bất đồng bộ qua Kafka tới notification-service
        userEventPublisher.publishUserRegistered(user, otp);
    }

    @Override
    @Transactional
    public JwtRes login(LoginReq req) {
        Authentication authentication;

        try {
            authentication = manager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        } catch (AuthenticationException e) {
            throw new BadRequestException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy thông tin tài khoản"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AuthException("Tài khoản chưa được kích hoạt. Vui lòng xác thực mã OTP gửi về email của bạn.");
        }
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AuthException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        // Tạo Refresh Token
        String refreshTokenStr = UUID.randomUUID().toString();
        RefreshToken token = new RefreshToken(
                null,
                refreshTokenStr,
                Instant.now().plus(7, ChronoUnit.DAYS),
                false,
                user
        );
        refreshTokenRepository.save(token);

        return new JwtRes(
                jwtUtils.generateToken(user),
                refreshTokenStr,
                "Bearer",
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
        );
    }

    @Override
    @Transactional
    public JwtRes refresh(RefreshReq req) {
        RefreshToken token = refreshTokenRepository.findByToken(req.refreshToken())
                .orElseThrow(() -> new NotFoundException("Refresh token không tồn tại"));

        if (token.getExpiresAt().isBefore(Instant.now()) || token.isRevoked()) {
            throw new BadRequestException("Refresh token đã hết hạn hoặc bị thu hồi");
        }

        // Thu hồi token cũ (Refresh token rotation - FR-IAM-011)
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        // Cấp token mới
        String newRefreshTokenStr = UUID.randomUUID().toString();
        RefreshToken newToken = new RefreshToken(
                null,
                newRefreshTokenStr,
                Instant.now().plus(7, ChronoUnit.DAYS),
                false,
                token.getUser()
        );
        refreshTokenRepository.save(newToken);

        return new JwtRes(
                jwtUtils.generateToken(token.getUser()),
                newRefreshTokenStr,
                "Bearer",
                token.getUser().getRoles().stream().map(r -> r.getRoleName().name()).toList()
        );
    }

    @Override
    @Transactional
    public String activeUser(ActiveUserReq req) {
        String identifier = req.getEffectiveIdentifier();
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByLoginIdentifier(identifier))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản với: " + identifier));

        if (user.getStatus() == UserStatus.ACTIVE) {
            return "Tài khoản đã được kích hoạt trước đó.";
        }

        if (user.getOtpExpiration() == null || user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            String newOtp = String.valueOf((int) ((Math.random() * 900000) + 100000));
            LocalDateTime newExpiration = LocalDateTime.now().plusMinutes(5);
            user.setOtpCode(newOtp);
            user.setOtpExpiration(newExpiration);
            userRepository.save(user);

            String toEmail = user.getEmail() != null && !user.getEmail().isBlank() ? user.getEmail() : user.getLoginIdentifier();
            if (toEmail != null && toEmail.contains("@")) {
                userEventPublisher.publishOtpEmail(toEmail, user.getFullName(), newOtp, "RESEND_OTP");
            }

            throw new AuthException("Mã OTP đã hết hạn! Hệ thống đã tự động gửi mã OTP mới về email của bạn.");
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(req.getOtp())) {
            throw new AuthException("Mã OTP không chính xác");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setOtpCode(null);
        user.setOtpExpiration(null);
        userRepository.save(user);

        return "Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.";
    }

    @Override
    public void logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            tokenBlacklistService.blacklistToken(token);
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public void forgotPassword(com.example.identityservice.models.dto.req.ForgotPasswordReq req) {
        userRepository.findByEmail(req.getEmail())
                .or(() -> userRepository.findByLoginIdentifier(req.getEmail()))
                .ifPresent(user -> {
                    String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
                    user.setOtpCode(otp);
                    user.setOtpExpiration(LocalDateTime.now().plusMinutes(5));
                    userRepository.save(user);

                    String toEmail = user.getEmail() != null && !user.getEmail().isBlank() ? user.getEmail() : user.getLoginIdentifier();
                    if (toEmail != null && toEmail.contains("@")) {
                        userEventPublisher.publishOtpEmail(toEmail, user.getFullName(), otp, "FORGOT_PASSWORD");
                    }
                });
    }

    @Override
    @Transactional
    public void resetPassword(com.example.identityservice.models.dto.req.ResetPasswordReq req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("Mã OTP không chính xác hoặc đã hết hạn"));

        if (user.getOtpExpiration() == null || user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Mã OTP đã hết hạn");
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(req.getOtp())) {
            throw new BadRequestException("Mã OTP không chính xác");
        }

        user.setPasswordHash(encoder.encode(req.getNewPassword()));
        user.setOtpCode(null);
        user.setOtpExpiration(null);
        userRepository.save(user);
    }
}
