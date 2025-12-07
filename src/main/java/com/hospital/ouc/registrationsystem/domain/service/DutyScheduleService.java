package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorDutySchedule;
import com.hospital.ouc.registrationsystem.web.dto.DutyScheduleDTO;

import java.util.List;

public interface DutyScheduleService {
    // 根据科室ID查询值班记录
    List<DoctorDutySchedule> getDutyScheduleByDeptId(Long departmentId);

    // 新增值班记录
    DoctorDutySchedule addDutySchedule(DutyScheduleDTO dto);

    // 编辑值班记录
    DoctorDutySchedule updateDutySchedule(DutyScheduleDTO dto);

    // 删除单条值班记录
    void deleteDutySchedule(Long id);

    // 批量删除科室所有值班记录
    void batchDeleteDutyScheduleByDeptId(Long departmentId);

    // 新增：查询所有值班记录（用于患者端展示）
    List<DoctorDutySchedule> getAllDutySchedules();

    // 查询某位医生的周末值班记录（周六/周日）
    List<DoctorDutySchedule> getDutySchedulesByDoctorId(Long doctorProfileId);

    // 新：按可选过滤条件查询值班记录（部门、周末类型、时段）
    List<DoctorDutySchedule> findByFilters(Long departmentId, Integer weekendType, String dutyTimeslot);
}
