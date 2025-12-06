// 值班时段枚举
package com.hospital.ouc.registrationsystem.domain.enums;

import lombok.Getter;

@Getter
public enum DutyTimeslot {
    MORNING("MORNING", "早班"),
    AFTERNOON("AFTERNOON", "中班"),
    NIGHT("NIGHT", "夜班");

    private final String code;
    private final String desc;

    DutyTimeslot(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 根据编码获取枚举
    public static DutyTimeslot getByCode(String code) {
        for (DutyTimeslot slot : values()) {
            if (slot.getCode().equals(code)) {
                return slot;
            }
        }
        throw new IllegalArgumentException("无效的值班时段：" + code);
    }
}