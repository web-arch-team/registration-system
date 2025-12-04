package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;
import java.util.List;

@Data
public class DoctorScheduleDTO {
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private String departmentName;

    private List<ScheduleItemDTO> schedules;
}