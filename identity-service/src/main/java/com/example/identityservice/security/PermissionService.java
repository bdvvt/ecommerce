package com.example.identityservice.security;

import com.example.identityservice.models.entities.Permission;
import com.example.identityservice.models.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service hỗ trợ kiểm tra quyền động trong Security (theo pm-xuatban).
 * Dùng được trong @PreAuthorize("@permissionService.hasPermission('users.view')")
 */
@Service("permissionService")
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Set<String> getPermissionsByLogin(String login) {
        if (login == null || login.isBlank()) {
            return Collections.emptySet();
        }

        return userRepository.findByUsernameWithRolesAndPermissions(login)
                .map(u -> u.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .filter(perm -> perm.getDeletedAt() == null && perm.getName() != null)
                        .map(Permission::getName)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    @Transactional(readOnly = true)
    public Set<String> getPermissionsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        return userRepository.findByIdWithRolesAndPermissions(userId)
                .map(u -> u.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .filter(perm -> perm.getDeletedAt() == null && perm.getName() != null)
                        .map(Permission::getName)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    /**
     * Kiểm tra user hiện tại có quyền cụ thể hay không.
     * Ví dụ: @PreAuthorize("@permissionService.hasPermission('users.view')")
     */
    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permission) || "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /**
     * Kiểm tra user hiện tại có ít nhất một trong các quyền.
     * Ví dụ: @PreAuthorize("@permissionService.hasAnyPermission('users.view', 'users.edit')")
     */
    public boolean hasAnyPermission(String... permissions) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Set<String> requiredPerms = Arrays.stream(permissions).collect(Collectors.toSet());
        return authentication.getAuthorities().stream()
                .anyMatch(a -> requiredPerms.contains(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
