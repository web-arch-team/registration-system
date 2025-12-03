package com.hospital.ouc.registrationsystem.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import com.hospital.ouc.registrationsystem.domain.enums.Gender;

/**
 * 患者档案实体，对应数据库表 patient_profile。
 * 保存患者的基础信息，并与统一用户表（app_user）一对一关联。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "patient_profile")
public class PatientProfile {
    /**
     * 主键ID（自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的统一用户（唯一、非空），对应 app_user.id
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    /**
     * 身份证号（唯一、非空）
     */
    @Column(name = "id_card", nullable = false, unique = true, length = 18)
    private String idCard;

    /**
     * 姓名（非空）
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 手机号（唯一、非空）
     */
    @Column(name = "phone_number", nullable = false, unique = true, length = 15)
    private String phoneNumber;

    /**
     * 年龄（可空）
     */
    private Integer age;

    /**
     * 性别（非空），数据库为 gender_enum，使用 Java 枚举并以字符串持久化（male/female）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender; // male | female

    /**
     * 是否有效（软删除标记）。删除时不物理删除，而是将该字段设为 false。
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
