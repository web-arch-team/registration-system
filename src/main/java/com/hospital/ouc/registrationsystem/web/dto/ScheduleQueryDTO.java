package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

@Data
public class ScheduleQueryDTO {
    private Long doctorId;
    private Integer weekday;
}