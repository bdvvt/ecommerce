package com.example.identityservice.models.repositories;

import com.example.identityservice.models.constants.RoleName;
import com.example.identityservice.models.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý Role theo chuẩn kiến trúc pm-xuatban-backend.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r WHERE r.deletedAt IS NULL ORDER BY r.roleId ASC")
    List<Role> findAll();

    @Query("SELECT r FROM Role r WHERE r.roleId = :id AND r.deletedAt IS NULL")
    Optional<Role> findActiveById(@Param("id") Long id);

    @Query("SELECT r FROM Role r WHERE r.roleName = :roleName AND r.deletedAt IS NULL")
    Optional<Role> findActiveByRoleName(@Param("roleName") RoleName roleName);

    Optional<Role> findByRoleName(RoleName roleName);

    Optional<Role> findByPermissionCode(String permissionCode);

    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions p WHERE r.roleId = :roleId AND r.deletedAt IS NULL")
    Optional<Role> findByIdWithPermissions(@Param("roleId") Long roleId);

    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions p WHERE r.roleName = :roleName AND r.deletedAt IS NULL")
    Optional<Role> findWithPermissionsByRoleName(@Param("roleName") RoleName roleName);

    boolean existsByRoleNameAndDeletedAtIsNull(RoleName roleName);
}
