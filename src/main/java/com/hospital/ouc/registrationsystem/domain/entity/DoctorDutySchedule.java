package com.hospital.ouc.registrationsystem.domain.entity;

import com.hospital.ouc.registrationsystem.domain.enums.DutyTimeslot;
import com.hospital.ouc.registrationsystem.domain.enums.WeekendType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "doctor_duty_schedule")
public class DoctorDutySchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_profile_id", nullable = false)
    private DoctorProfile doctorProfile;

    @Column(name = "weekend_type", nullable = false)
    private Integer weekendType; // 6=周六，7=周日（关联WeekendType枚举）

    @Column(name = "duty_timeslot", nullable = false)
    private String dutyTimeslot; // MORNING/AFTERNOON/NIGHT（关联DutyTimeslot枚举）

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // 便捷方法：转换为枚举类型
    public WeekendType getWeekendTypeEnum() {
        return WeekendType.getByCode(this.weekendType);
    }

    public DutyTimeslot getDutyTimeslotEnum() {
        return DutyTimeslot.getByCode(this.dutyTimeslot);
    }

    // 设置枚举类型
    public void setWeekendTypeEnum(WeekendType weekendType) {
        this.weekendType = weekendType.getCode();
    }

    public void setDutyTimeslotEnum(DutyTimeslot dutyTimeslot) {
        this.dutyTimeslot = dutyTimeslot.getCode();
    }
}