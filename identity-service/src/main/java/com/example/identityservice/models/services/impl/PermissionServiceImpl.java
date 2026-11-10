package com.example.identityservice.models.services.impl;

import com.example.identityservice.exceptions.BadRequestException;
import com.example.identityservice.exceptions.NotFoundException;
import com.example.identityservice.models.dto.res.PermissionRes;
import com.example.identityservice.models.entities.Permission;
import com.example.identityservice.models.entities.Role;
import com.example.identityservice.models.entities.User;
import com.example.identityservice.models.repositories.UserRepository;
import com.example.identityservice.models.services.IPermissionService;
import com.example.identityservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Triển khai PermissionService chuẩn theo kiến trúc pm-xuatban-backend.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements IPermissionService {

    private final UserRepository userRepository;

    private Long resolveCurrentUserId() {
        Long userId = SecurityUtils.getLoggedUserId();
        if (userId != null) {
            return userId;
        }
        String login = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (login != null) {
            return userRepository.findByUsername(login)
                    .map(User::getUserId)
                    .orElse(null);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionRes> getPermissionsByFeatureName(String featureName) {
        final Long userId = resolveCurrentUserId();
        if (userId == null) {
            throw new BadRequestException("Không tìm thấy thông tin phiên đăng nhập của người dùng");
        }

        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với id: " + userId));

        Set<Role> roles = user.getRoles() != null ? user.getRoles() : Collections.emptySet();

        // Lấy tất cả permissions từ roles của user và filter theo feature name (chuẩn pm-xuatban)
        String featurePrefix = featureName + ".";
        List<Permission> permissions = roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .filter(p -> p.getDeletedAt() == null)
                .filter(p -> p.getName() != null && p.getName().startsWith(featurePrefix))
                .distinct()
                .sorted(Comparator.comparing(Permission::getName))
                .toList();

        return permissions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PermissionRes toResponse(Permission permission) {
        return PermissionRes.builder()
                .id(permission.getId())
                .name(permission.getName())
                .displayName(permission.getDisplayName())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}
