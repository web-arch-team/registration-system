package com.hospital.ouc.registrationsystem.web.dto;

import com.hospital.ouc.registrationsystem.domain.enums.DutyTimeslot;
import com.hospital.ouc.registrationsystem.domain.enums.WeekendType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DutyScheduleDTO {
    private Long id; // 编辑时必填

    @NotNull(message = "科室ID不能为空")
    private Long departmentId;

    @NotNull(message = "医生ID不能为空")
    private Long doctorProfileId;

    @NotNull(message = "周末类型不能为空（仅支持周六/周日）")
    private WeekendType weekendType; // 6=周六，7=周日

    @NotNull(message = "值班时段不能为空")
    private DutyTimeslot dutyTimeslot; // 早班/中班/夜班
}
