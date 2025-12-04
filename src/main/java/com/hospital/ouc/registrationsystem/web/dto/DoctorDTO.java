package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

@Data
public class DoctorDTO {
    private Long id;
    private String doctorId;
    private String name;
    private String title;
    private Integer age;
    private String gender;
    private Long departmentId;
    private String departmentName;
}