package com.hospital.ouc.registrationsystem.web;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorDutySchedule;
import com.hospital.ouc.registrationsystem.domain.service.DutyScheduleService;
import com.hospital.ouc.registrationsystem.web.dto.DoctorDutyVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient/duty-schedules")
@RequiredArgsConstructor
public class PatientDutyController {

    private final DutyScheduleService dutyScheduleService;
    private static final Logger log = LoggerFactory.getLogger(PatientDutyController.class);

    /** 获取所有值班记录（可选按 departmentId 过滤） */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DoctorDutyVO>> list(@RequestParam(required = false) Long departmentId,
                                                    @RequestParam(required = false) Integer weekendType,
                                                    @RequestParam(required = false) String dutyTimeslot) {
        // debug incoming params
        log.debug("list called with departmentId={}, weekendType={}, dutyTimeslot={}", departmentId, weekendType, dutyTimeslot);
        // delegate to service that will perform DB-level filtering
        List<DoctorDutySchedule> schedules = dutyScheduleService.findByFilters(departmentId, weekendType, dutyTimeslot);
        log.info("Returning {} duty schedules for departmentId={} weekendType={} dutyTimeslot={}", schedules.size(), departmentId, weekendType, dutyTimeslot);
        // map to VO to avoid exposing JPA entities / lazy loading issues
        List<DoctorDutyVO> vos = schedules.stream().map(d -> {
            DoctorDutyVO v = new DoctorDutyVO();
            v.setId(d.getId());
            if (d.getDepartment() != null) {
                v.setDepartmentId(d.getDepartment().getId());
                v.setDepartmentName(d.getDepartment().getDepartmentName());
            }
            if (d.getDoctorProfile() != null) {
                v.setDoctorId(d.getDoctorProfile().getId());
                v.setDoctorName(d.getDoctorProfile().getName());
                v.setDoctorTitle(d.getDoctorProfile().getTitle());
            }
            v.setWeekendType(d.getWeekendType());
            v.setDutyTimeslot(d.getDutyTimeslot());
            return v;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(vos);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DoctorDutyVO>> all() {
        List<DoctorDutySchedule> schedules = dutyScheduleService.getAllDutySchedules();
        log.info("/api/patient/duty-schedules/all returning {} entries", schedules.size());
        List<DoctorDutyVO> vos = schedules.stream().map(d -> {
            DoctorDutyVO v = new DoctorDutyVO();
            v.setId(d.getId());
            if (d.getDepartment() != null) {
                v.setDepartmentId(d.getDepartment().getId());
                v.setDepartmentName(d.getDepartment().getDepartmentName());
            }
            if (d.getDoctorProfile() != null) {
                v.setDoctorId(d.getDoctorProfile().getId());
                v.setDoctorName(d.getDoctorProfile().getName());
                v.setDoctorTitle(d.getDoctorProfile().getTitle());
            }
            v.setWeekendType(d.getWeekendType());
            v.setDutyTimeslot(d.getDutyTimeslot());
            return v;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(vos);
    }
}
