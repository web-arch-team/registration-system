package com.hospital.ouc.registrationsystem.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class DepartmentDTO {
    private Long id;

    @NotBlank(message = "科室名称不能为空")
    private String departmentName;

    // 新增：可携带该科室的疾病列表（新增时可提供，查询时返回）
    private List<DiseaseDTO> diseases;
}