package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

@Data
public class DiseaseTimetableItemDTO {
    private Long doctorProfileId;   // 医生档案ID
    private String doctorId;        // 医生工号
    private String doctorName;
    private String doctorTitle;
    private String departmentName;
    private Integer weekday;        // 1-5
    private String timeslot;        // AM1..PM4
    private String timeDescription; // 上午/下午具体时段
    private Integer currentPatients;
    private Integer maxPatients;
    private Boolean available;
}
