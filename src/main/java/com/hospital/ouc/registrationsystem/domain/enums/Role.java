package com.hospital.ouc.registrationsystem.domain.enums;

/**
 * 系统角色枚举：患者、医生、管理员。
 * 与数据库中 app_user.role 的取值一致，使用字符串方式持久化。
 */
public enum Role {
    PATIENT,
    DOCTOR,
    ADMIN
}

