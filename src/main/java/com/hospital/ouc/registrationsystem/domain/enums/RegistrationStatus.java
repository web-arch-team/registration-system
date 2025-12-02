package com.hospital.ouc.registrationsystem.domain.enums;

/**
 * 挂号状态枚举，用于 patient_doctor_registration.status 字段。
 * 可根据业务扩展；当前建议值：PENDING / PAID / CANCELLED / COMPLETED。
 */
public enum RegistrationStatus {
    PENDING,
    PAID,
    CANCELLED,
    COMPLETED
}

