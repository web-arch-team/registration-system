package com.hospital.ouc.registrationsystem.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class DoctorUpdateDTO {
    private String username;
    private String password;
    private String doctorId;
    private String name;
    private Integer age;

    @Pattern(regexp = "^(male|female)$", message = "性别必须为male或female")
    private String gender;

    private String title;
    private Long departmentId;
    private List<Long> diseaseIds;
    private Boolean isActive;
}
