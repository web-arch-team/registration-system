package com.hospital.ouc.registrationsystem.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class DoctorDTO {
    // 医生档案ID（后端生成）
    private Long id;

    // 登录用户名（仅 admin 侧新增/修改医生时使用）
    @NotBlank(message = "用户名不能为空")
    private String username;

    // 登录密码（仅 admin 新增医生时必填，更新时可选）
    @NotBlank(message = "密码不能为空")
    private String password;

    // 医生工号
    @NotBlank(message = "医生工号不能为空")
    private String doctorId;

    // 医生姓名
    @NotBlank(message = "医生姓名不能为空")
    private String name;

    private Integer age;

    // 性别：male / female
    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^(male|female)$", message = "性别必须为male或female")
    private String gender;

    // 职称
    private String title;

    // 所属科室ID
    @NotNull(message = "科室ID不能为空")
    private Long departmentId;

    // 所属科室名称（后端填充）
    private String departmentName;

    // 是否在职/可用
    private boolean isActive;

    // 该医生可诊断的疾病ID列表
    private List<Long> diseaseIds;
}
