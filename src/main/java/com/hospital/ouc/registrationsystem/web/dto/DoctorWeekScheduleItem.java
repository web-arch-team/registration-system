package com.hospital.ouc.registrationsystem.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医生一周排班的单个条目。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWeekScheduleItem {
    private Integer weekday;   // 1-5
    private String timeslot;   // AM1..PM4
    private String department; // 科室名称
}
