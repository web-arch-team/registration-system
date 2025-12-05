package com.hospital.ouc.registrationsystem.domain.service.impl;

import com.hospital.ouc.registrationsystem.web.dto.ScheduleDTO;
import com.hospital.ouc.registrationsystem.domain.entity.Department;
import com.hospital.ouc.registrationsystem.domain.entity.DoctorDepartmentSchedule;
import com.hospital.ouc.registrationsystem.domain.entity.DoctorProfile;
import com.hospital.ouc.registrationsystem.domain.enums.ResultCodeEnum;
import com.hospital.ouc.registrationsystem.domain.enums.TimeSlot;
import com.hospital.ouc.registrationsystem.domain.service.BusinessException;
import com.hospital.ouc.registrationsystem.domain.repository.DepartmentRepository;
import com.hospital.ouc.registrationsystem.domain.repository.DoctorDepartmentScheduleRepository;
import com.hospital.ouc.registrationsystem.domain.repository.DoctorProfileRepository;
import com.hospital.ouc.registrationsystem.domain.service.ScheduleManageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 管理员排班管理服务实现类
 * 修复点：
 * 1. 手动参数校验替代注解校验
 * 2. String类型timeslot转换为TimeSlot枚举
 * 3. 统一异常抛出逻辑
 */
@Service
@RequiredArgsConstructor
public class ScheduleManageServiceImpl implements ScheduleManageService {

    // 注入仓库依赖
    private final DoctorDepartmentScheduleRepository scheduleRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * 查询指定科室的所有排班记录
     */
    @Override
    public List<DoctorDepartmentSchedule> getScheduleByDeptId(Long departmentId) {
        // 手动校验：科室ID不能为空
        if (departmentId == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "科室ID不能为空");
        }
        // 校验科室是否存在
        if (!departmentRepository.existsById(departmentId)) {
            throw new BusinessException(ResultCodeEnum.DEPARTMENT_NOT_FOUND);
        }
        // 查询并返回科室下所有排班
        return scheduleRepository.findByDepartmentId(departmentId);
    }

    /**
     * 新增排班
     */
    @Override
    @Transactional(rollbackOn = Exception.class) // 异常时回滚事务
    public DoctorDepartmentSchedule addSchedule(ScheduleDTO scheduleDTO) {
        // ========== 1. 全量手动参数校验 ==========
        // 医生ID校验
        if (scheduleDTO.getDoctorProfileId() == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "医生ID不能为空");
        }
        // 科室ID校验
        if (scheduleDTO.getDepartmentId() == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "科室ID不能为空");
        }
        // 星期校验（1-5）
        if (scheduleDTO.getWeekday() == null || scheduleDTO.getWeekday() < 1 || scheduleDTO.getWeekday() > 5) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "星期只能是1-5（周一到周五）");
        }
        // 时间段校验
        TimeSlot timeSlot;
        try {
            if (scheduleDTO.getTimeslot() == null || scheduleDTO.getTimeslot().trim().isEmpty()) {
                throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "时间段不能为空");
            }
            // 转换String到TimeSlot枚举
            timeSlot = TimeSlot.fromString(scheduleDTO.getTimeslot().trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), e.getMessage());
        }

        // 最大挂号数校验（不传则默认2，传则必须≥1）
        Integer maxPatients = scheduleDTO.getMaxPatientsPerSlot();
        if (maxPatients == null) {
            maxPatients = 2; // 默认值
        } else if (maxPatients < 1) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "最大挂号数不能小于1");
        }

        // ========== 2. 业务规则校验 ==========
        // 校验医生是否存在（且未被删除）
        DoctorProfile doctor = doctorProfileRepository.findById(scheduleDTO.getDoctorProfileId())
                .orElseThrow(() -> new BusinessException(ResultCodeEnum.DOCTOR_NOT_FOUND));
        // 校验医生是否激活（is_active=true）
        if (!doctor.getIsActive()) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "该医生已被禁用，无法排班");
        }

        // 校验科室是否存在
        Department department = departmentRepository.findById(scheduleDTO.getDepartmentId())
                .orElseThrow(() -> new BusinessException(ResultCodeEnum.DEPARTMENT_NOT_FOUND));

        // 校验：该医生在该星期+时间段是否已有排班（唯一约束）
        Optional<DoctorDepartmentSchedule> existSchedule = scheduleRepository
                .findByDoctorProfileIdAndWeekdayAndTimeslot(
                        scheduleDTO.getDoctorProfileId(),
                        scheduleDTO.getWeekday(),
                        timeSlot
                );
        if (existSchedule.isPresent()) {
            throw new BusinessException(ResultCodeEnum.DUPLICATE_SCHEDULE);
        }

        // ========== 3. 构建并保存排班 ==========
        DoctorDepartmentSchedule schedule = new DoctorDepartmentSchedule();
        schedule.setDoctorProfile(doctor);
        schedule.setDepartment(department);
        schedule.setWeekday(scheduleDTO.getWeekday());
        schedule.setTimeslot(timeSlot);
        schedule.setMaxPatientsPerSlot(maxPatients);

        return scheduleRepository.save(schedule);
    }

    /**
     * 修改排班
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public DoctorDepartmentSchedule updateSchedule(ScheduleDTO scheduleDTO) {
        // ========== 1. 全量手动参数校验 ==========
        // 排班ID校验
        if (scheduleDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "排班ID不能为空");
        }
        // 医生ID校验
        if (scheduleDTO.getDoctorProfileId() == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "医生ID不能为空");
        }
        // 科室ID校验
        if (scheduleDTO.getDepartmentId() == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "科室ID不能为空");
        }
        // 星期校验（1-5）
        if (scheduleDTO.getWeekday() == null || scheduleDTO.getWeekday() < 1 || scheduleDTO.getWeekday() > 5) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "星期只能是1-5（周一到周五）");
        }
        // 时间段校验 + String转TimeSlot枚举
        TimeSlot timeSlot;
        try {
            if (scheduleDTO.getTimeslot() == null || scheduleDTO.getTimeslot().trim().isEmpty()) {
                throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "时间段不能为空");
            }
            timeSlot = TimeSlot.fromString(scheduleDTO.getTimeslot().trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), e.getMessage());
        }

        // 最大挂号数校验（不传则沿用原有值，传则必须≥1）
        Integer maxPatients = scheduleDTO.getMaxPatientsPerSlot();
        if (maxPatients != null && maxPatients < 1) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "最大挂号数不能小于1");
        }

        // ========== 2. 业务规则校验 ==========
        // 校验排班记录是否存在
        DoctorDepartmentSchedule existSchedule = scheduleRepository.findById(scheduleDTO.getId())
                .orElseThrow(() -> new BusinessException(ResultCodeEnum.SCHEDULE_NOT_FOUND));

        // 校验医生是否存在（且未被删除）
        DoctorProfile doctor = doctorProfileRepository.findById(scheduleDTO.getDoctorProfileId())
                .orElseThrow(() -> new BusinessException(ResultCodeEnum.DOCTOR_NOT_FOUND));
        if (!doctor.getIsActive()) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "该医生已被禁用，无法排班");
        }

        // 校验科室是否存在
        Department department = departmentRepository.findById(scheduleDTO.getDepartmentId())
                .orElseThrow(() -> new BusinessException(ResultCodeEnum.DEPARTMENT_NOT_FOUND));

        // 校验：修改后的医生+星期+时间段是否重复（排除当前排班ID）
        Optional<DoctorDepartmentSchedule> duplicateSchedule = scheduleRepository
                .findByDoctorProfileIdAndWeekdayAndTimeslot(
                        scheduleDTO.getDoctorProfileId(),
                        scheduleDTO.getWeekday(),
                        timeSlot // 正确：传TimeSlot枚举
                );
        if (duplicateSchedule.isPresent() && !duplicateSchedule.get().getId().equals(scheduleDTO.getId())) {
            throw new BusinessException(ResultCodeEnum.DUPLICATE_SCHEDULE);
        }

        // ========== 3. 更新并保存排班 ==========
        existSchedule.setDoctorProfile(doctor);
        existSchedule.setDepartment(department);
        existSchedule.setWeekday(scheduleDTO.getWeekday());
        existSchedule.setTimeslot(timeSlot);
        // 仅当传入值不为空时更新，否则保留原有值
        if (maxPatients != null) {
            existSchedule.setMaxPatientsPerSlot(maxPatients);
        }

        return scheduleRepository.save(existSchedule);
    }

    /**
     * 删除单个排班（物理删除，也可改为逻辑删除）
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteSchedule(Long scheduleId) {
        // 校验排班ID不能为空
        if (scheduleId == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "排班ID不能为空");
        }
        // 校验排班是否存在
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new BusinessException(ResultCodeEnum.SCHEDULE_NOT_FOUND);
        }
        // 删除排班
        scheduleRepository.deleteById(scheduleId);
    }

    /**
     * 批量删除指定科室的所有排班
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void batchDeleteScheduleByDeptId(Long departmentId) {
        // 校验科室ID不能为空
        if (departmentId == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "科室ID不能为空");
        }
        // 校验科室是否存在
        if (!departmentRepository.existsById(departmentId)) {
            throw new BusinessException(ResultCodeEnum.DEPARTMENT_NOT_FOUND);
        }
        // 批量删除科室下所有排班
        scheduleRepository.deleteByDepartmentId(departmentId);
    }
}