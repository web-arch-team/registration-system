package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.*;
import com.hospital.ouc.registrationsystem.domain.enums.RegistrationStatus;
import com.hospital.ouc.registrationsystem.domain.enums.TimeSlot;
import com.hospital.ouc.registrationsystem.domain.repository.*;
import com.hospital.ouc.registrationsystem.web.dto.RegistrationRequestDTO;
import com.hospital.ouc.registrationsystem.web.dto.RegistrationResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientRegistrationService {

    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DiseaseRepository diseaseRepository;
    private final DoctorDepartmentScheduleRepository scheduleRepository;
    private final DoctorDiseaseRepository doctorDiseaseRepository;
    private final PatientDoctorRegistrationRepository registrationRepository;

    private static final int DEFAULT_MAX_PER_SLOT = 2;

    public PatientRegistrationService(PatientProfileRepository patientProfileRepository,
                                      DoctorProfileRepository doctorProfileRepository,
                                      DiseaseRepository diseaseRepository,
                                      DoctorDepartmentScheduleRepository scheduleRepository,
                                      DoctorDiseaseRepository doctorDiseaseRepository,
                                      PatientDoctorRegistrationRepository registrationRepository) {
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.diseaseRepository = diseaseRepository;
        this.scheduleRepository = scheduleRepository;
        this.doctorDiseaseRepository = doctorDiseaseRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public RegistrationResponseDTO register(RegistrationRequestDTO req) {
        PatientProfile patient = patientProfileRepository.findById(req.getPatientProfileId())
                .orElseThrow(() -> new RuntimeException("患者不存在"));
        DoctorProfile doctor = doctorProfileRepository.findById(req.getDoctorProfileId())
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        Disease disease = diseaseRepository.findById(req.getDiseaseId())
                .orElseThrow(() -> new RuntimeException("疾病不存在"));

        // 校验医生是否能诊疗该疾病
        if (!doctorDiseaseRepository.existsByDoctorProfileIdAndDiseaseId(doctor.getId(), disease.getId())) {
            throw new RuntimeException("该医生不支持此疾病");
        }

        // 校验排班是否匹配
        TimeSlot slot = TimeSlot.valueOf(req.getTimeslot());
        List<DoctorDepartmentSchedule> schedules = scheduleRepository.findByDoctorProfileIdAndWeekday(doctor.getId(), req.getWeekday());
        DoctorDepartmentSchedule targetSchedule = schedules.stream()
                .filter(s -> s.getTimeslot() == slot)
                .findFirst()
                .orElse(null);
        if (targetSchedule == null) {
            throw new RuntimeException("该医生在所选时段无排班");
        }

        int max = targetSchedule.getMaxPatientsPerSlot() != null ? targetSchedule.getMaxPatientsPerSlot() : DEFAULT_MAX_PER_SLOT;
        long current = registrationRepository.countByDoctorProfileIdAndWeekdayAndTimeslotAndStatusIn(
                doctor.getId(),
                req.getWeekday(),
                slot,
                List.of(RegistrationStatus.PAID, RegistrationStatus.PENDING, RegistrationStatus.COMPLETED)
        );
        if (current >= max) {
            throw new RuntimeException("该时段号源已满");
        }

        PatientDoctorRegistration registration = PatientDoctorRegistration.builder()
                .patientProfile(patient)
                .doctorProfile(doctor)
                .disease(disease)
                .weekday(req.getWeekday())
                .timeslot(slot)
                .status(RegistrationStatus.PAID) // 直接设为已支付/已确认
                .build();
        PatientDoctorRegistration saved = registrationRepository.save(registration);

        RegistrationResponseDTO resp = new RegistrationResponseDTO();
        resp.setId(saved.getId());
        resp.setPatientProfileId(patient.getId());
        resp.setDoctorProfileId(doctor.getId());
        resp.setDiseaseId(disease.getId());
        resp.setWeekday(saved.getWeekday());
        resp.setTimeslot(saved.getTimeslot().name());
        resp.setStatus(saved.getStatus().name());
        return resp;
    }
}
