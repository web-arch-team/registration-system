package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorDutySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorDutyScheduleRepository extends JpaRepository<DoctorDutySchedule, Long> {
    // 根据科室ID查询值班记录
    List<DoctorDutySchedule> findByDepartmentId(Long departmentId);

    // 检查同一科室、同一周末类型、同一时段是否已存在值班记录
    boolean existsByDepartmentIdAndWeekendTypeAndDutyTimeslot(Long departmentId, Integer weekendType, String dutyTimeslot);

    // 根据科室ID批量删除
    void deleteByDepartmentId(Long departmentId);
}
