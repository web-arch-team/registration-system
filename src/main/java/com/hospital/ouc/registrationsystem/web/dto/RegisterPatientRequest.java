package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

/**
 * 患者注册请求 DTO。
 */
@Data
public class RegisterPatientRequest {
    private String username;
    private String password;
    private String name;
    private String idCard;
    private String phoneNumber;
    private Integer age;
    private String gender; // "male" / "female"
}

