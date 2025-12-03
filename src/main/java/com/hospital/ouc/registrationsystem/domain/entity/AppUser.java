package com.hospital.ouc.registrationsystem.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import com.hospital.ouc.registrationsystem.domain.enums.Role;

import java.time.LocalDateTime;

/**
 * 统一用户表实体，对应数据库表 app_user。
 * 包含系统三类用户（患者、医生、管理员）的基础登录信息。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_user")
public class AppUser {
    /**
     * 主键ID（自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名（唯一、非空），用于登录
     */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /**
     * 密码（非空），建议保存为加密后的字符串
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * 角色（非空）：PATIENT / DOCTOR / ADMIN，使用 Java 枚举并以字符串持久化
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * 账户创建时间（由数据库默认值生成，应用侧只读）
     */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 是否有效（软删除标记）。删除时不物理删除，而是将该字段设为 false。
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
