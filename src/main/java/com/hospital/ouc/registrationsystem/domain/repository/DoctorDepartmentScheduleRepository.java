package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorDepartmentSchedule;
import com.hospital.ouc.registrationsystem.domain.enums.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorDepartmentScheduleRepository extends JpaRepository<DoctorDepartmentSchedule, Long> {
    /**
     * 根据医生工号查询一周内的全部排班记录。
     */
    List<DoctorDepartmentSchedule> findByDoctorProfile_DoctorId(String doctorId);

    // 根据医生ID查找排班
    List<DoctorDepartmentSchedule> findByDoctorProfileId(Long doctorId);

    // 根据科室ID查找排班
    List<DoctorDepartmentSchedule> findByDepartmentId(Long departmentId);

    // 根据医生ID和星期查找排班
    List<DoctorDepartmentSchedule> findByDoctorProfileIdAndWeekday(Long doctorId, Integer weekday);

    // 根据医生ID列表查询全部排班
    List<DoctorDepartmentSchedule> findByDoctorProfileIdIn(List<Long> doctorIds);

    // 根据医生ID列表 + 星期 查询排班
    List<DoctorDepartmentSchedule> findByDoctorProfileIdInAndWeekday(List<Long> doctorIds, Integer weekday);

    /**
     * 根据医生ID、星期、时间段查询排班（用于唯一约束校验）
     */
    Optional<DoctorDepartmentSchedule> findByDoctorProfileIdAndWeekdayAndTimeslot(
            Long doctorProfileId,
            Integer weekday,
            TimeSlot timeslot
    );

    void deleteByDepartmentIdAndDoctorProfileId(Long departmentId, Long doctorProfileId);
    void deleteByDepartmentId(Long departmentId);
}
