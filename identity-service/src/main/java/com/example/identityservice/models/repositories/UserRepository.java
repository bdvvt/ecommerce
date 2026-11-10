package com.example.identityservice.models.repositories;

import com.example.identityservice.models.constants.RoleName;
import com.example.identityservice.models.constants.UserStatus;
import com.example.identityservice.models.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginIdentifier(String loginIdentifier);
    Optional<User> findFirstByEmailOrLoginIdentifier(String email, String loginIdentifier);

    @Query("SELECT u FROM User u WHERE u.loginIdentifier = :username OR u.email = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions p " +
           "WHERE u.userId = :id")
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") Long id);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions p " +
           "WHERE u.loginIdentifier = :username OR u.email = :username")
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);

    Optional<User> findByEmail(String email);

    boolean existsByLoginIdentifier(String loginIdentifier);
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.loginIdentifier = :username OR u.email = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE (:role IS NULL OR r.roleName = :role) AND (:status IS NULL OR u.status = :status)")
    List<User> findAllByRoleAndStatus(@Param("role") RoleName role, @Param("status") UserStatus status);

    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE (:role IS NULL OR r.roleName = :role) AND (:status IS NULL OR u.status = :status)",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u LEFT JOIN u.roles r WHERE (:role IS NULL OR r.roleName = :role) AND (:status IS NULL OR u.status = :status)")
    Page<User> findAllByRoleAndStatus(@Param("role") RoleName role, @Param("status") UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.fullName LIKE concat('%',:search,'%') OR u.loginIdentifier LIKE concat('%',:search,'%') OR u.email LIKE concat('%',:search,'%')")
    List<User> findAllBySearch(@Param("search") String search);

    @Query(value = "SELECT DISTINCT u FROM User u WHERE u.fullName LIKE concat('%',:search,'%') OR u.loginIdentifier LIKE concat('%',:search,'%') OR u.email LIKE concat('%',:search,'%')",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u WHERE u.fullName LIKE concat('%',:search,'%') OR u.loginIdentifier LIKE concat('%',:search,'%') OR u.email LIKE concat('%',:search,'%')")
    Page<User> findAllBySearch(@Param("search") String search, Pageable pageable);
}
