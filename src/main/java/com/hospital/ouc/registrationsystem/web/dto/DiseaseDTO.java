package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

@Data
public class DiseaseDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Long departmentId;
    private String departmentName;
}