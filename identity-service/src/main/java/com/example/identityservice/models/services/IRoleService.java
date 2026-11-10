package com.example.identityservice.models.services;

import com.example.identityservice.models.dto.req.RoleReq;
import com.example.identityservice.models.dto.res.PermissionRes;
import com.example.identityservice.models.dto.res.RoleRes;

import java.util.List;
import java.util.Set;

/**
 * Service quản lý danh mục vai trò (Role) theo chuẩn kiến trúc pm-xuatban-backend.
 */
public interface IRoleService {

    List<RoleRes> getRoles();

    RoleRes getRoleById(Long id);

    RoleRes createRole(RoleReq request);

    RoleRes updateRole(Long id, RoleReq request);

    void deleteRole(Long id);

    List<PermissionRes> getPermissionsByRoleId(Long roleId);

    RoleRes assignPermissionsToRole(Long roleId, Set<Long> permissionIds);
}
