package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

/**
 * 患者注册响应 DTO。
 */
@Data
public class RegisterPatientResponse {
    private Long userId;
    private Long patientId;
    private String username;
    private String role; // PATIENT
}

