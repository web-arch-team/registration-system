package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.PatientDoctorRegistration;
import com.hospital.ouc.registrationsystem.domain.entity.PatientProfile;
import com.hospital.ouc.registrationsystem.domain.repository.PatientDoctorRegistrationRepository;
import com.hospital.ouc.registrationsystem.web.dto.DoctorDayScheduleItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorScheduleService {

    private final PatientDoctorRegistrationRepository registrationRepository;

    public DoctorScheduleService(PatientDoctorRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
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
}
