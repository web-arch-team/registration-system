package com.hospital.ouc.registrationsystem.domain.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    DUPLICATE_SCHEDULE(409, "该医生在该时间段已有排班，无法重复添加"),
    SCHEDULE_NOT_FOUND(404, "排班记录不存在"),
    DOCTOR_NOT_FOUND(404, "医生不存在"),
    DEPARTMENT_NOT_FOUND(404, "科室不存在"),
    SYSTEM_ERROR(500, "系统异常");

    private final Integer code;
    private final String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
