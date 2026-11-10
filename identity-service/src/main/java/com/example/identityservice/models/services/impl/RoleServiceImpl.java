package com.example.identityservice.models.services.impl;

import com.example.identityservice.exceptions.BadRequestException;
import com.example.identityservice.exceptions.NotFoundException;
import com.example.identityservice.models.constants.RoleName;
import com.example.identityservice.models.dto.mapper.RoleMapper;
import com.example.identityservice.models.dto.req.RoleReq;
import com.example.identityservice.models.dto.res.PermissionRes;
import com.example.identityservice.models.dto.res.RoleRes;
import com.example.identityservice.models.entities.Permission;
import com.example.identityservice.models.entities.Role;
import com.example.identityservice.models.repositories.PermissionRepository;
import com.example.identityservice.models.repositories.RoleRepository;
import com.example.identityservice.models.services.IRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Triển khai RoleService chuẩn theo kiến trúc pm-xuatban-backend.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleRes> getRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleRes getRoleById(Long id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .or(() -> roleRepository.findActiveById(id))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vai trò với id: " + id));
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleRes createRole(RoleReq request) {
        if (roleRepository.existsByRoleNameAndDeletedAtIsNull(request.getRoleName())) {
            throw new BadRequestException("Vai trò '" + request.getRoleName() + "' đã tồn tại");
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            permissions.addAll(permissionRepository.findAllById(request.getPermissionIds()));
        }

        Role role = Role.builder()
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .permissionCode(request.getPermissionCode() != null && !request.getPermissionCode().isBlank()
                        ? request.getPermissionCode()
                        : request.getRoleName().name())
                .permissions(permissions)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        Role saved = roleRepository.save(role);
        log.info("Đã tạo mới Role: {} (ID: {})", saved.getRoleName(), saved.getRoleId());
        return roleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RoleRes updateRole(Long id, RoleReq request) {
        Role role = roleRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vai trò với id: " + id));

        if (role.getRoleName() != request.getRoleName() &&
                roleRepository.existsByRoleNameAndDeletedAtIsNull(request.getRoleName())) {
            throw new BadRequestException("Tên vai trò '" + request.getRoleName() + "' đã tồn tại");
        }

        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        if (request.getPermissionCode() != null && !request.getPermissionCode().isBlank()) {
            role.setPermissionCode(request.getPermissionCode());
        }

        if (request.getPermissionIds() != null) {
            List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
            role.getPermissions().clear();
            role.getPermissions().addAll(permissions);
        }

        role.setUpdatedAt(LocalDate.now());
        Role saved = roleRepository.save(role);
        log.info("Đã cập nhật Role ID: {}", id);
        return roleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vai trò với id: " + id));

        if (role.getRoleName() == RoleName.ROLE_ADMIN) {
            throw new BadRequestException("Không thể xóa vai trò quản trị hệ thống (ROLE_ADMIN)!");
        }

        role.setDeletedAt(new Date());
        role.setUpdatedAt(LocalDate.now());
        roleRepository.save(role);
        log.info("Đã xóa mềm vai trò ID: {} ({})", id, role.getRoleName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionRes> getPermissionsByRoleId(Long roleId) {
        roleRepository.findActiveById(roleId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vai trò với id: " + roleId));

        List<Permission> permissions = permissionRepository.findByRoleId(roleId);

        return permissions.stream()
                .map(p -> PermissionRes.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .displayName(p.getDisplayName())
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleRes assignPermissionsToRole(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vai trò với id: " + roleId));

        Set<Long> idsToAssign = permissionIds != null ? permissionIds : Collections.emptySet();
        List<Permission> permissions = permissionRepository.findAllById(idsToAssign);

        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        role.setUpdatedAt(LocalDate.now());

        Role saved = roleRepository.save(role);
        log.info("Đã gán {} quyền cho Role ID: {}", permissions.size(), roleId);
        return roleMapper.toResponse(saved);
    }
}
