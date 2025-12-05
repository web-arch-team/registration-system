package com.hospital.ouc.registrationsystem.web;

import com.hospital.ouc.registrationsystem.domain.service.PatientScheduleService;
import com.hospital.ouc.registrationsystem.web.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class PatientScheduleController {

    private final PatientScheduleService scheduleService;

    public PatientScheduleController(PatientScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 获取所有科室
     */
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> departments = scheduleService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    /**
     * 根据科室ID获取疾病列表
     */
    @GetMapping("/department/{departmentId}/diseases")
    public ResponseEntity<List<DiseaseDTO>> getDiseasesByDepartment(@PathVariable Long departmentId) {
        List<DiseaseDTO> diseases = scheduleService.getDiseasesByDepartment(departmentId);
        return ResponseEntity.ok(diseases);
    }

    /**
     * 根据疾病ID获取医生列表
     */
    @GetMapping("/disease/{diseaseId}/doctors")
    public ResponseEntity<List<DoctorDTO>> getDoctorsByDisease(@PathVariable Long diseaseId) {
        List<DoctorDTO> doctors = scheduleService.getDoctorsByDisease(diseaseId);
        return ResponseEntity.ok(doctors);
    }

    /**
     * 获取某疾病对应医生的排班（timeslot × weekday）。
     * weekday 可选，不传则返回一周排班；传 1-5 则仅返回该日。
     */
    @GetMapping("/disease/{diseaseId}/timetable")
    public ResponseEntity<List<DiseaseTimetableItemDTO>> getDiseaseTimetable(
            @PathVariable Long diseaseId,
            @RequestParam(required = false) Integer weekday) {
        List<DiseaseTimetableItemDTO> items = scheduleService.getDiseaseTimetable(diseaseId, weekday);
        return ResponseEntity.ok(items);
    }

    /**
     * 根据医生ID获取排班
     */
    @GetMapping("/doctor/{doctorId}/schedules")
    public ResponseEntity<DoctorScheduleDTO> getSchedulesByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) Integer weekday) {
        DoctorScheduleDTO schedules = scheduleService.getSchedulesByDoctor(doctorId, weekday);
        return ResponseEntity.ok(schedules);
    }

    /**
     * 获取医生的所有排班（按星期分组）
     */
    @GetMapping("/doctor/{doctorId}/schedules/grouped")
    public ResponseEntity<Map<Integer, List<ScheduleItemDTO>>> getSchedulesGroupedByWeekday(
            @PathVariable Long doctorId) {
        Map<Integer, List<ScheduleItemDTO>> groupedSchedules =
                scheduleService.getSchedulesByDoctorGroupedByWeekday(doctorId);
        return ResponseEntity.ok(groupedSchedules);
    }

    /**
     * 根据科室ID获取医生列表
     */
    @GetMapping("/department/{departmentId}/doctors")
    public ResponseEntity<List<DoctorDTO>> getDoctorsByDepartment(@PathVariable Long departmentId) {
        List<DoctorDTO> doctors = scheduleService.getDoctorsByDepartment(departmentId);
        return ResponseEntity.ok(doctors);
    }
}
