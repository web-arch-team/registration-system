package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;
import com.hospital.ouc.registrationsystem.domain.enums.TimeSlot;

@Data
public class ScheduleItemDTO {
    private Long scheduleId;
    private Integer weekday;
    private TimeSlot timeslot;
    private Integer currentPatients;
    private Integer maxPatients = 10;
    private Boolean available;
    // 时间段描述
    private String timeDescription;
}