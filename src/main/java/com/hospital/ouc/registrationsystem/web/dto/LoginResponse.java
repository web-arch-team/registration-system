package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 登录响应 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String username;
    private String role;
    /**
     * 当角色为 DOCTOR 时返回医生工号，便于前端自动绑定医生身份。
     */
    private String doctorId;
    /**
     * 当角色为 PATIENT 时返回患者档案ID，便于前端挂号。
     */
    private Long patientId;
}
