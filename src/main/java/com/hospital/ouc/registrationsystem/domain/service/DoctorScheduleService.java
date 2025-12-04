package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.PatientDoctorRegistration;
import com.hospital.ouc.registrationsystem.domain.entity.PatientProfile;
import com.hospital.ouc.registrationsystem.domain.repository.DoctorDepartmentScheduleRepository;
import com.hospital.ouc.registrationsystem.domain.repository.PatientDoctorRegistrationRepository;
import com.hospital.ouc.registrationsystem.web.dto.DoctorDayScheduleItem;
import com.hospital.ouc.registrationsystem.web.dto.DoctorWeekScheduleItem;
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
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 查询医生本周排班（周一到周五，每天 8 个时段）。
     */
    public List<DoctorWeekScheduleItem> getWeekSchedule(String doctorId) {
        return scheduleRepository.findByDoctorProfile_DoctorId(doctorId).stream()
                .map(s -> new DoctorWeekScheduleItem(
                        s.getWeekday(),
                        s.getTimeslot().name(),
                        s.getDepartment() == null ? "" : s.getDepartment().getDepartmentName()
                ))
                .collect(Collectors.toList());
    }
}
