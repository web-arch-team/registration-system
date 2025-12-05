package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.web.dto.ScheduleDTO;
import com.hospital.ouc.registrationsystem.domain.entity.DoctorDepartmentSchedule;

import java.util.List;

public interface ScheduleManageService {

    /**
     * 查询指定科室的所有排班记录
     */
    List<DoctorDepartmentSchedule> getScheduleByDeptId(Long departmentId);

    /**
     * 新增排班
     */
    DoctorDepartmentSchedule addSchedule(ScheduleDTO scheduleDTO);

    /**
     * 修改排班
     */
    DoctorDepartmentSchedule updateSchedule(ScheduleDTO scheduleDTO);

    /**
     * 删除排班
     */
    void deleteSchedule(Long scheduleId);

    /**
     * 批量删除指定科室的排班（可选）
     */
    void batchDeleteScheduleByDeptId(Long departmentId);
}
