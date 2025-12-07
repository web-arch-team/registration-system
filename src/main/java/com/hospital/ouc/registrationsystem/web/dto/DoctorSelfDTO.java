package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class DoctorSelfDTO {
    private Long id;
    private String username;
    private String doctorId;
    private String name;
    private Integer age;
    private String gender;
    private String title;
    private Long departmentId;
    private String departmentName;
    private List<DiseaseDTO> diseases;
}

