package com.hospital.ouc.registrationsystem.domain.service.impl;

import com.hospital.ouc.registrationsystem.domain.entity.Department;
import com.hospital.ouc.registrationsystem.domain.entity.DoctorDutySchedule;
import com.hospital.ouc.registrationsystem.domain.entity.DoctorProfile;
import com.hospital.ouc.registrationsystem.domain.enums.DutyTimeslot;
import com.hospital.ouc.registrationsystem.domain.enums.WeekendType;
import com.hospital.ouc.registrationsystem.domain.repository.DepartmentRepository;
import com.hospital.ouc.registrationsystem.domain.repository.DoctorDutyScheduleRepository;
import com.hospital.ouc.registrationsystem.domain.repository.DoctorProfileRepository;
import com.hospital.ouc.registrationsystem.domain.service.DutyScheduleService;
import com.hospital.ouc.registrationsystem.web.dto.DutyScheduleDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DutyScheduleServiceImpl implements DutyScheduleService {

    private final DoctorDutyScheduleRepository dutyScheduleRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @Override
    public List<DoctorDutySchedule> getDutyScheduleByDeptId(Long departmentId) {
        return dutyScheduleRepository.findByDepartmentId(departmentId);
    }

    @Override
    @Transactional
    public DoctorDutySchedule addDutySchedule(DutyScheduleDTO dto) {
        // 1. 验证科室存在
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("科室不存在，ID：" + dto.getDepartmentId()));

        // 2. 验证医生存在
        DoctorProfile doctor = doctorProfileRepository.findById(dto.getDoctorProfileId())
                .orElseThrow(() -> new EntityNotFoundException("医生不存在，ID：" + dto.getDoctorProfileId()));

        // 3. 验证是否重复值班（同一科室+周末类型+时段只能有一个医生）
        if (dutyScheduleRepository.existsByDepartmentIdAndWeekendTypeAndDutyTimeslot(
                dto.getDepartmentId(),
                dto.getWeekendType().getCode(),
                dto.getDutyTimeslot().getCode()
        )) {
            throw new IllegalArgumentException(
                    String.format("%s %s已存在值班医生，请勿重复添加",
                            dto.getWeekendType().getDesc(),
                            dto.getDutyTimeslot().getDesc())
            );
        }

        // 4. 构建实体并保存
        DoctorDutySchedule dutySchedule = new DoctorDutySchedule();
        dutySchedule.setDepartment(department);
        dutySchedule.setDoctorProfile(doctor);
        dutySchedule.setWeekendTypeEnum(dto.getWeekendType());
        dutySchedule.setDutyTimeslotEnum(dto.getDutyTimeslot());

        return dutyScheduleRepository.save(dutySchedule);
    }

    @Override
    @Transactional
    public DoctorDutySchedule updateDutySchedule(DutyScheduleDTO dto) {
        // 1. 验证值班记录存在
        DoctorDutySchedule existing = dutyScheduleRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("值班记录不存在，ID：" + dto.getId()));

        // 2. 验证科室和医生存在
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("科室不存在，ID：" + dto.getDepartmentId()));
        DoctorProfile doctor = doctorProfileRepository.findById(dto.getDoctorProfileId())
                .orElseThrow(() -> new EntityNotFoundException("医生不存在，ID：" + dto.getDoctorProfileId()));

        // 3. 若修改了周末类型/时段/科室，需检查重复
        boolean isChanged = !existing.getWeekendType().equals(dto.getWeekendType().getCode())
                || !existing.getDutyTimeslot().equals(dto.getDutyTimeslot().getCode())
                || !existing.getDepartment().getId().equals(dto.getDepartmentId());

        if (isChanged && dutyScheduleRepository.existsByDepartmentIdAndWeekendTypeAndDutyTimeslot(
                dto.getDepartmentId(),
                dto.getWeekendType().getCode(),
                dto.getDutyTimeslot().getCode()
        )) {
            throw new IllegalArgumentException(
                    String.format("%s %s已存在值班医生，请勿重复添加",
                            dto.getWeekendType().getDesc(),
                            dto.getDutyTimeslot().getDesc())
            );
        }

        // 4. 更新字段
        existing.setDepartment(department);
        existing.setDoctorProfile(doctor);
        existing.setWeekendTypeEnum(dto.getWeekendType());
        existing.setDutyTimeslotEnum(dto.getDutyTimeslot());

        return dutyScheduleRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteDutySchedule(Long id) {
        if (!dutyScheduleRepository.existsById(id)) {
            throw new EntityNotFoundException("值班记录不存在，ID：" + id);
        }
        dutyScheduleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDeleteDutyScheduleByDeptId(Long departmentId) {
        dutyScheduleRepository.deleteByDepartmentId(departmentId);
    }
}
