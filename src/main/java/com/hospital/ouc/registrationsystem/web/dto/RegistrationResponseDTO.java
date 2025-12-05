package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

/**
 * 挂号响应 DTO。
 */
@Data
public class RegistrationResponseDTO {
    private Long id;
    private Long patientProfileId;
    private Long doctorProfileId;
    private Long diseaseId;
    private Integer weekday;
    private String timeslot;
    private String status;
}
