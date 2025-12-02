package com.hospital.ouc.registrationsystem.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import com.hospital.ouc.registrationsystem.domain.enums.Gender;

/**
 * 医生档案实体，对应数据库表 doctor_profile。
 * 保存医生的基础信息，并与统一用户表（app_user）一对一关联；
 * 同时关联所属科室（department）。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "doctor_profile")
public class DoctorProfile {
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
     * 医生工号（唯一、非空），例如 D00001
     */
    @Column(name = "doctor_id", nullable = false, unique = true, length = 10)
    private String doctorId;

    /**
     * 医生姓名（非空）
     */
    @Column(nullable = false, length = 100)
    private String name;

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
     * 职称（可空），例如“主任医师”、“主治医师”等
     */
    @Column(length = 100)
    private String title;

    /**
     * 所属科室（可空），多对一关联 department
     */
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}
