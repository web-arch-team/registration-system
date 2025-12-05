package com.hospital.ouc.registrationsystem.web;

import com.hospital.ouc.registrationsystem.web.dto.ResultDTO;
import com.hospital.ouc.registrationsystem.web.dto.ScheduleDTO;
import com.hospital.ouc.registrationsystem.domain.entity.DoctorDepartmentSchedule;
import com.hospital.ouc.registrationsystem.domain.service.ScheduleManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员排班管理接口
 * 仅允许ADMIN角色访问
 */
@RestController
@RequestMapping("/api/admin/schedule")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 权限控制：仅管理员可访问
public class AdminScheduleController {

    private final ScheduleManageService scheduleManageService;

    /**
     * 查询指定科室的所有排班
     */
    @GetMapping("/department/{deptId}")
    public ResultDTO<List<DoctorDepartmentSchedule>> getScheduleByDeptId(@PathVariable Long deptId) {
        List<DoctorDepartmentSchedule> scheduleList = scheduleManageService.getScheduleByDeptId(deptId);
        return ResultDTO.success(scheduleList);
    }

    /**
     * 新增排班
     */
    @PostMapping
    public ResultDTO<DoctorDepartmentSchedule> addSchedule(@Valid @RequestBody ScheduleDTO scheduleDTO) {
        DoctorDepartmentSchedule schedule = scheduleManageService.addSchedule(scheduleDTO);
        return ResultDTO.success(schedule);
    }

    /**
     * 修改排班
     */
    @PutMapping
    public ResultDTO<DoctorDepartmentSchedule> updateSchedule(@Valid @RequestBody ScheduleDTO scheduleDTO) {
        DoctorDepartmentSchedule schedule = scheduleManageService.updateSchedule(scheduleDTO);
        return ResultDTO.success(schedule);
    }

    /**
     * 删除单个排班
     */
    @DeleteMapping("/{scheduleId}")
    public ResultDTO<Void> deleteSchedule(@PathVariable Long scheduleId) {
        scheduleManageService.deleteSchedule(scheduleId);
        return ResultDTO.success(null);
    }

    /**
     * 批量删除指定科室的所有排班（可选）
     */
    @DeleteMapping("/batch/department/{deptId}")
    public ResultDTO<Void> batchDeleteScheduleByDeptId(@PathVariable Long deptId) {
        scheduleManageService.batchDeleteScheduleByDeptId(deptId);
        return ResultDTO.success(null);
    }
}
