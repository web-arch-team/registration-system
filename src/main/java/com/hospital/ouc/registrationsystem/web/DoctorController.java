package com.hospital.ouc.registrationsystem.web;

import com.hospital.ouc.registrationsystem.domain.service.DoctorScheduleService;
import com.hospital.ouc.registrationsystem.web.dto.DoctorDayScheduleItem;
import com.hospital.ouc.registrationsystem.web.dto.DoctorWeekScheduleItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorScheduleService doctorScheduleService;

    public DoctorController(DoctorScheduleService doctorScheduleService) {
        this.doctorScheduleService = doctorScheduleService;
    }

    /**
     * 查询医生在指定工作日的日程（8个时段），返回每个时段的患者信息与时段名。
     */
    @GetMapping("/schedule")
    public ResponseEntity<List<DoctorDayScheduleItem>> getDaySchedule(
            @RequestParam String doctorId,
            @RequestParam Integer weekday) {
        return ResponseEntity.ok(doctorScheduleService.getDaySchedule(doctorId, weekday));
    }

    /**
     * 查询医生本周排班（周一到周五，每日 8 个时段）。
     */
    @GetMapping("/week-schedule")
    public ResponseEntity<List<DoctorWeekScheduleItem>> getWeekSchedule(
            @RequestParam String doctorId) {
        return ResponseEntity.ok(doctorScheduleService.getWeekSchedule(doctorId));
    }
}
