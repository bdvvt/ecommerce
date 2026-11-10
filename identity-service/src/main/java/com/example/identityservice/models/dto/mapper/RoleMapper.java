package com.example.identityservice.models.dto.mapper;

import com.example.identityservice.models.dto.res.PermissionRes;
import com.example.identityservice.models.dto.res.RoleRes;
import com.example.identityservice.models.entities.Permission;
import com.example.identityservice.models.entities.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "roleId", source = "roleId")
    @Mapping(target = "roleName", expression = "java(role.getRoleName() != null ? role.getRoleName().name() : null)")
    @Mapping(target = "permissionCode", source = "permissionCode")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "permissions", source = "permissions", qualifiedByName = "mapPermissions")
    RoleRes toResponse(Role role);

    @Named("mapPermissions")
    default List<PermissionRes> mapPermissions(Set<Permission> permissions) {
        if (permissions == null) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .filter(p -> p.getDeletedAt() == null)
                .map(p -> PermissionRes.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .displayName(p.getDisplayName())
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build())
                .sorted((p1, p2) -> {
                    if (p1.getName() == null) return -1;
                    if (p2.getName() == null) return 1;
                    return p1.getName().compareTo(p2.getName());
                })
                .collect(Collectors.toList());
    }
}
