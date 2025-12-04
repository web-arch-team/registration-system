package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.*;
import com.hospital.ouc.registrationsystem.domain.repository.*;
import com.hospital.ouc.registrationsystem.web.dto.*;
import com.hospital.ouc.registrationsystem.domain.enums.TimeSlot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PatientScheduleService {

    private final DepartmentRepository departmentRepository;
    private final DiseaseRepository diseaseRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorDiseaseRepository doctorDiseaseRepository;
    private final DoctorDepartmentScheduleRepository scheduleRepository;

    public PatientScheduleService(
            DepartmentRepository departmentRepository,
            DiseaseRepository diseaseRepository,
            DoctorProfileRepository doctorProfileRepository,
            DoctorDiseaseRepository doctorDiseaseRepository,
            DoctorDepartmentScheduleRepository scheduleRepository) {
        this.departmentRepository = departmentRepository;
        this.diseaseRepository = diseaseRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.doctorDiseaseRepository = doctorDiseaseRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // 获取所有科室
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::convertToDepartmentDTO)
                .collect(Collectors.toList());
    }

    // 根据科室ID获取该科室负责的疾病
    public List<DiseaseDTO> getDiseasesByDepartment(Long departmentId) {
        return diseaseRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDiseaseDTO)
                .collect(Collectors.toList());
    }

    // 根据疾病ID获取可以治疗该疾病的医生
    public List<DoctorDTO> getDoctorsByDisease(Long diseaseId) {
        List<DoctorDisease> doctorDiseases = doctorDiseaseRepository.findByDiseaseId(diseaseId);

        return doctorDiseases.stream()
                .map(DoctorDisease::getDoctorProfile)
                .filter(doctor -> doctor.getIsActive()) // 只返回有效的医生
                .map(this::convertToDoctorDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取某疾病对应医生在本周的排班（timeslot × weekday 交集）。
     */
    public List<DiseaseTimetableItemDTO> getDiseaseTimetable(Long diseaseId, Integer weekday) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new RuntimeException("疾病不存在"));

        // 能诊疗该疾病的医生
        List<Long> doctorIds = doctorDiseaseRepository.findByDiseaseId(diseaseId).stream()
                .map(dd -> dd.getDoctorProfile().getId())
                .collect(Collectors.toList());
        if (doctorIds.isEmpty()) {
            return List.of();
        }

        // 排班查询：若传 weekday 则仅查该天
        List<DoctorDepartmentSchedule> schedules;
        if (weekday != null) {
            schedules = scheduleRepository.findByDoctorProfileIdInAndWeekday(doctorIds, weekday);
        } else {
            schedules = scheduleRepository.findByDoctorProfileIdIn(doctorIds);
        }

        // 只保留科室匹配当前疾病科室的排班
        Long deptId = disease.getDepartment().getId();
        schedules = schedules.stream()
                .filter(s -> s.getDepartment() != null && deptId.equals(s.getDepartment().getId()))
                .collect(Collectors.toList());

        return schedules.stream()
                .map(s -> {
                    DoctorProfile doc = s.getDoctorProfile();
                    DiseaseTimetableItemDTO dto = new DiseaseTimetableItemDTO();
                    dto.setDoctorProfileId(doc.getId());
                    dto.setDoctorId(doc.getDoctorId());
                    dto.setDoctorName(doc.getName());
                    dto.setDoctorTitle(doc.getTitle());
                    dto.setDepartmentName(s.getDepartment() == null ? null : s.getDepartment().getDepartmentName());
                    dto.setWeekday(s.getWeekday());
                    dto.setTimeslot(s.getTimeslot().name());
                    dto.setTimeDescription(getTimeDescription(s.getTimeslot()));
                    dto.setAvailable(true);
                    dto.setCurrentPatients(0);
                    dto.setMaxPatients(100); // 约定每医生每天16个号
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 根据医生ID获取该医生的排班情况
    public DoctorScheduleDTO getSchedulesByDoctor(Long doctorId, Integer weekday) {
        Optional<DoctorProfile> doctorOpt = doctorProfileRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            throw new RuntimeException("医生不存在");
        }

        DoctorProfile doctor = doctorOpt.get();
        DoctorScheduleDTO dto = new DoctorScheduleDTO();
        dto.setDoctorId(doctor.getId());
        dto.setDoctorName(doctor.getName());
        dto.setDoctorTitle(doctor.getTitle());

        if (doctor.getDepartment() != null) {
            dto.setDepartmentName(doctor.getDepartment().getDepartmentName());
        }

        // 获取医生的排班
        List<DoctorDepartmentSchedule> schedules;
        if (weekday != null) {
            schedules = scheduleRepository.findByDoctorProfileIdAndWeekday(doctorId, weekday);
        } else {
            schedules = scheduleRepository.findByDoctorProfileId(doctorId);
        }

        // 转换为DTO
        List<ScheduleItemDTO> scheduleItems = schedules.stream()
                .map(this::convertToScheduleItemDTO)
                .collect(Collectors.toList());

        dto.setSchedules(scheduleItems);
        return dto;
    }

    // 获取医生的所有排班
    public Map<Integer, List<ScheduleItemDTO>> getSchedulesByDoctorGroupedByWeekday(Long doctorId) {
        List<DoctorDepartmentSchedule> allSchedules = scheduleRepository.findByDoctorProfileId(doctorId);

        // 按星期分组
        Map<Integer, List<ScheduleItemDTO>> groupedSchedules = new HashMap<>();

        for (DoctorDepartmentSchedule schedule : allSchedules) {
            Integer weekday = schedule.getWeekday();
            ScheduleItemDTO item = convertToScheduleItemDTO(schedule);

            groupedSchedules.computeIfAbsent(weekday, k -> new ArrayList<>()).add(item);
        }

        return groupedSchedules;
    }

    // 根据科室ID直接获取该科室的医生
    public List<DoctorDTO> getDoctorsByDepartment(Long departmentId) {
        return doctorProfileRepository.findByDepartmentIdAndIsActiveTrue(departmentId).stream()
                .map(this::convertToDoctorDTO)
                .collect(Collectors.toList());
    }

    // 转换方法
    private DepartmentDTO convertToDepartmentDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setDepartmentName(department.getDepartmentName());
        return dto;
    }

    private DiseaseDTO convertToDiseaseDTO(Disease disease) {
        DiseaseDTO dto = new DiseaseDTO();
        dto.setId(disease.getId());
        dto.setName(disease.getName());
        dto.setCode(disease.getCode());
        dto.setDescription(disease.getDescription());
        dto.setDepartmentId(disease.getDepartment().getId());
        dto.setDepartmentName(disease.getDepartment().getDepartmentName());
        return dto;
    }

    private DoctorDTO convertToDoctorDTO(DoctorProfile doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setDoctorId(doctor.getDoctorId());
        dto.setName(doctor.getName());
        dto.setTitle(doctor.getTitle());
        dto.setAge(doctor.getAge());
        dto.setGender(doctor.getGender().name());

        if (doctor.getDepartment() != null) {
            dto.setDepartmentId(doctor.getDepartment().getId());
            dto.setDepartmentName(doctor.getDepartment().getDepartmentName());
        }

        return dto;
    }

    private ScheduleItemDTO convertToScheduleItemDTO(DoctorDepartmentSchedule schedule) {
        ScheduleItemDTO dto = new ScheduleItemDTO();
        dto.setScheduleId(schedule.getId());
        dto.setWeekday(schedule.getWeekday());
        dto.setTimeslot(schedule.getTimeslot());
        dto.setTimeDescription(getTimeDescription(schedule.getTimeslot()));

        // 这里可以根据实际业务统计已挂号人数
        // 目前先简单设置为0，实际项目中需要查询挂号记录表
        dto.setCurrentPatients(0);
        dto.setAvailable(dto.getCurrentPatients() < dto.getMaxPatients());

        return dto;
    }

    private String getTimeDescription(TimeSlot timeslot) {
        switch (timeslot) {
            case AM1: return "上午 8:00-9:00";
            case AM2: return "上午 9:00-10:00";
            case AM3: return "上午 10:00-11:00";
            case AM4: return "上午 11:00-12:00";
            case PM1: return "下午 14:00-15:00";
            case PM2: return "下午 15:00-16:00";
            case PM3: return "下午 16:00-17:00";
            case PM4: return "下午 17:00-18:00";
            default: return "未知时间段";
        }
    }
}
