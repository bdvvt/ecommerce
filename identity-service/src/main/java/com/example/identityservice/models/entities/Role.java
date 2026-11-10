package com.example.identityservice.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import com.example.identityservice.models.constants.RoleName;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", unique = true, nullable = false)
    private RoleName roleName;

    @Column(name = "description")
    private String description;

    @Column(name = "permission_code")
    private String permissionCode;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "deleted_at")
    private Date deletedAt;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY, cascade = {})
    @JsonIgnore
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "permission_role",
            joinColumns = @JoinColumn(
                    name = "role_id",
                    referencedColumnName = "role_id",
                    foreignKey = @ForeignKey(name = "fk_permission_role_role")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "permission_id",
                    referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "fk_permission_role_permission")
            )
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    public Role(Long roleId, RoleName roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.permissionCode = roleName != null ? roleName.name() : null;
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    public Role(Long roleId, RoleName roleName, String permissionCode) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.permissionCode = permissionCode;
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }
}
