package com.hospital.ouc.registrationsystem.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import com.hospital.ouc.registrationsystem.domain.enums.TimeSlot;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 医生-科室-排班实体，对应数据库表 doctor_department_schedule。
 * 用于记录医生在某个科室、某一天、某一时段的排班情况。
 * 对 (doctor_profile_id, weekday, timeslot) 建立唯一约束，避免重复排班。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "doctor_department_schedule",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_doctor_weekday_timeslot", columnNames = {"doctor_profile_id", "weekday", "timeslot"})
        }
)
public class DoctorDepartmentSchedule {
    /**
     * 主键ID（自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 医生档案（非空），多对一关联 doctor_profile
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_profile_id")
    private DoctorProfile doctorProfile;

    /**
     * 科室（非空），多对一关联 department
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    /**
     * 星期（非空），范围 1-5；由数据库 CHECK 约束保证
     */
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer weekday;

    /**
     * 时间槽（非空），数据库为 time_slot，使用 Java 枚举并以字符串持久化（AM1..AM4���PM1..PM4）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private TimeSlot timeslot; // AM1..AM4, PM1..PM4
}
