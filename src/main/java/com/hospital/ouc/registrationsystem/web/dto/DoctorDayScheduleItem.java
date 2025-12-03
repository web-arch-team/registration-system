package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

@Data
public class DoctorDayScheduleItem {
    private String timeslot; // AM1..PM4
    private Long patientId;
    private String patientName;
    private String patientIdCard;
    private String patientPhone;
    private Integer patientAge;
    private String patientGender; // male/female
}

