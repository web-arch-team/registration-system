package com.hospital.ouc.registrationsystem.web.dto;

import lombok.Data;

@Data
public class DoctorSearchCriteria {
    private Long id;
    private String doctorId;
    private String name;
    private String gender;
    private String title;
    private Long departmentId;
    /**
     * 表示是否查询已软删除的记录：
     *  - null 表示不按软删除过滤
     *  - true 表示查询已删除（isActive == false）
     *  - false 表示查询未删除（isActive == true）
     */
    private Boolean deleted;
}
