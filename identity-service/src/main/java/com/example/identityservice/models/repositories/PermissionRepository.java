package com.example.identityservice.models.repositories;

import com.example.identityservice.models.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý Permission theo chuẩn kiến trúc pm-xuatban-backend.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    @Query("SELECT DISTINCT p FROM Permission p " +
           "INNER JOIN p.roles r " +
           "WHERE r.roleId = :roleId AND p.deletedAt IS NULL AND r.deletedAt IS NULL")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT p FROM Permission p " +
           "WHERE p.name LIKE CONCAT(:featureName, '.%') AND p.deletedAt IS NULL " +
           "ORDER BY p.name ASC")
    List<Permission> findByFeatureName(@Param("featureName") String featureName);
}
