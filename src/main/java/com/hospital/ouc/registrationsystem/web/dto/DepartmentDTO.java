package com.hospital.ouc.registrationsystem.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentDTO {
    private Long id;

    @NotBlank(message = "科室名称不能为空")
    private String departmentName;
}