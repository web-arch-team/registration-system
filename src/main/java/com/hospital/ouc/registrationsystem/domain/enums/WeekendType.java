// 周末类型枚举（6=周六，7=周日）
package com.hospital.ouc.registrationsystem.domain.enums;

import lombok.Getter;

@Getter
public enum WeekendType {
    SATURDAY(6, "周六"),
    SUNDAY(7, "周日");

    private final Integer code;
    private final String desc;

    WeekendType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 根据编码获取枚举
    public static WeekendType getByCode(Integer code) {
        for (WeekendType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的周末类型：" + code);
    }
}