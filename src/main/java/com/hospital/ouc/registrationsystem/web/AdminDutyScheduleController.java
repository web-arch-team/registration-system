package com.hospital.ouc.registrationsystem.web;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorDutySchedule;
import com.hospital.ouc.registrationsystem.domain.service.DutyScheduleService;
import com.hospital.ouc.registrationsystem.web.dto.DutyScheduleDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/duty-schedule")
@RequiredArgsConstructor
@Tag(name = "管理员-值班管理接口", description = "周末值班表的增删改查")
@PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问
public class AdminDutyScheduleController {

    private final DutyScheduleService dutyScheduleService;

    @Operation(summary = "根据科室ID查询值班记录")
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<DoctorDutySchedule>> getDutyByDeptId(@PathVariable Long deptId) {
        return ResponseEntity.ok(dutyScheduleService.getDutyScheduleByDeptId(deptId));
    }

    @Operation(summary = "新增值班记录")
    @PostMapping
    public ResponseEntity<DoctorDutySchedule> addDuty(@Valid @RequestBody DutyScheduleDTO dto) {
        DoctorDutySchedule dutySchedule = dutyScheduleService.addDutySchedule(dto);
        return new ResponseEntity<>(dutySchedule, HttpStatus.CREATED);
    }

    @Operation(summary = "编辑值班记录")
    @PutMapping
    public ResponseEntity<DoctorDutySchedule> updateDuty(@Valid @RequestBody DutyScheduleDTO dto) {
        return ResponseEntity.ok(dutyScheduleService.updateDutySchedule(dto));
    }

    @Operation(summary = "删除单条值班记录")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDuty(@PathVariable Long id) {
        dutyScheduleService.deleteDutySchedule(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "批量删除科室所有值班记录")
    @DeleteMapping("/batch/department/{deptId}")
    public ResponseEntity<Void> batchDeleteDutyByDeptId(@PathVariable Long deptId) {
        dutyScheduleService.batchDeleteDutyScheduleByDeptId(deptId);
        return ResponseEntity.noContent().build();
    }
}
