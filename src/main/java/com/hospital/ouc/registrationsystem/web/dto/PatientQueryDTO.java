package com.hospital.ouc.registrationsystem.web.dto;

import com.hospital.ouc.registrationsystem.domain.enums.Gender;
import lombok.Data;

@Data
public class PatientQueryDTO {
    private String name; // 姓名模糊查询
    private String idCard; // 身份证精确查询
    private String phoneNumber; // 手机号精确查询
    private Gender gender; // 性别
    private Integer pageNum = 1; // 页码，默认1
    private Integer pageSize = 10; // 页大小，默认10
}
