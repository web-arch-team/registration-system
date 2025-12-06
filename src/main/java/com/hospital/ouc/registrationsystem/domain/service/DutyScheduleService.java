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
}
