package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

@Data
public class ScheduleDTO {
    // 排班ID（修改/删除时用）
    private Long id;

    // 医生档案ID
    private Long doctorProfileId;

    // 科室ID
    private Long departmentId;

    // 星期（1-5）
    private Integer weekday;

    // 时间段
    private String timeslot;

    // 每个时间段最大挂号数（不传则默认2）
    private Integer maxPatientsPerSlot;
}