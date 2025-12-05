package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

/**
 * 挂号请求 DTO。
 */
@Data
public class RegistrationRequestDTO {
    private Long patientProfileId;
    private Long doctorProfileId;
    private Long diseaseId;
    private Integer weekday; // 1-5
    private String timeslot; // AM1..PM4
}
