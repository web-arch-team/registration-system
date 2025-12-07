package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

@Data
public class DoctorDutySimpleDTO {
    private Long id;
    private Integer weekendType; // 6/7
    private String dutyTimeslot; // MORNING/AFTERNOON/NIGHT
    private Long departmentId;
    private String departmentName;
}

