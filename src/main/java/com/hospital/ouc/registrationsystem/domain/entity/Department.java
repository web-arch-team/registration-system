package com.hospital.ouc.registrationsystem.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 科室表实体，对应数据库表 department。
 * 用于维护医院的科室信息（如内科、外科等）。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "department")
public class Department {
    /**
     * 主键ID（自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 科室名称（非空），例如“内科”、“外科”
     */
    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;
}
