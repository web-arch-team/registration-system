package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.PatientDoctorRegistration;
import com.hospital.ouc.registrationsystem.domain.entity.PatientProfile;
import com.hospital.ouc.registrationsystem.domain.repository.DoctorDepartmentScheduleRepository;
import com.hospital.ouc.registrationsystem.domain.repository.DoctorProfileRepository;
import com.hospital.ouc.registrationsystem.domain.repository.PatientDoctorRegistrationRepository;
import com.hospital.ouc.registrationsystem.web.dto.DoctorDayScheduleItem;
import com.hospital.ouc.registrationsystem.web.dto.DoctorWeekScheduleItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorScheduleService {

    private final PatientDoctorRegistrationRepository registrationRepository;
    private final DoctorDepartmentScheduleRepository scheduleRepository;

    public DoctorScheduleService(PatientDoctorRegistrationRepository registrationRepository,
                                 DoctorDepartmentScheduleRepository scheduleRepository) {
        this.registrationRepository = registrationRepository;
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * 根据医生工号查询当天排班，工号由登录返回的 doctorId 自动传入。
     */
    public List<DoctorDayScheduleItem> getDaySchedule(String doctorId, Integer weekday) {
        List<PatientDoctorRegistration> regs = registrationRepository.findByDoctorIdAndWeekday(doctorId, weekday);
        return regs.stream().map(r -> {
            PatientProfile p = r.getPatientProfile();
            DoctorDayScheduleItem item = new DoctorDayScheduleItem();
            item.setTimeslot(r.getTimeslot().name());
            item.setPatientId(p.getId());
            item.setPatientName(p.getName());
            item.setPatientIdCard(p.getIdCard());
            item.setPatientPhone(p.getPhoneNumber());
            item.setPatientAge(p.getAge());
            item.setPatientGender(p.getGender().name());
            // 新增：根据挂号状态判断是否已取消
            try {
                item.setCanceled(r.getStatus() == com.hospital.ouc.registrationsystem.domain.enums.RegistrationStatus.CANCELLED);
                item.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
            } catch (Exception e) {
                item.setCanceled(false);
                item.setStatus(null);
            }
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 查询医生本周排班（周一到周五，每天 8 个时段）。
     */
    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    public List<DoctorWeekScheduleItem> getWeekSchedule(String doctorId) {

        // 1. 先查工号对应的 profileId
        var profile = doctorProfileRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        Long profileId = profile.getId();

        // 2. 用 profileId 查询排班
        return scheduleRepository.findByDoctorProfileId(profileId)
                .stream()
                .map(s -> new DoctorWeekScheduleItem(
                        s.getWeekday(),
                        s.getTimeslot().name(),
                        s.getDepartment() == null ? "" : s.getDepartment().getDepartmentName()
                ))
                .collect(Collectors.toList());
    }
}
