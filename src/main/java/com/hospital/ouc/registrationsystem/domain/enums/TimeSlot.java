package com.hospital.ouc.registrationsystem.domain.enums;

/**
 * 时间槽枚举，对应数据库 time_slot。
 * 取值：AM1, AM2, AM3, AM4, PM1, PM2, PM3, PM4。
 */
public enum TimeSlot {
    AM1, AM2, AM3, AM4,
    PM1, PM2, PM3, PM4;

    /**
     * 将String转换为TimeSlot枚举（兼容大小写）
     */
    public static TimeSlot fromString(String timeslotStr) {
        try {
            return TimeSlot.valueOf(timeslotStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "无效的时间段：" + timeslotStr +
                            "，仅支持AM1/AM2/AM3/AM4/PM1/PM2/PM3/PM4"
            );
        }
    }
}

