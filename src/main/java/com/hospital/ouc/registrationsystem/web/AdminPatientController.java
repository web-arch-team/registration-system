// src/main/java/com/hospital/ouc/registrationsystem/web/AdminPatientController.java
package com.hospital.ouc.registrationsystem.web;

import com.hospital.ouc.registrationsystem.web.dto.PatientDTO;
import com.hospital.ouc.registrationsystem.web.dto.PatientQueryDTO;
import com.hospital.ouc.registrationsystem.domain.service.PatientManagementService;
import com.hospital.ouc.registrationsystem.domain.validation.AddGroup;
import com.hospital.ouc.registrationsystem.domain.validation.UpdateGroup;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 管理员患者管理控制器：支持增删改查，修改时传啥改啥 */
@RestController
@RequestMapping("/api/admin/patients")
@RequiredArgsConstructor
public class AdminPatientController {

    private final PatientManagementService patientManagementService;

    /** 新增患者：强制校验所有必填字段 */
    @PostMapping
    public ResponseEntity<PatientDTO> addPatient(@Validated(AddGroup.class) @RequestBody PatientDTO patientDTO) {
        PatientDTO result = patientManagementService.addPatient(patientDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /** 分页查询患者：支持多条件筛选 */
    @GetMapping
    public ResponseEntity<Page<PatientDTO>> queryPatients(PatientQueryDTO queryDTO) {
        Page<PatientDTO> page = patientManagementService.queryPatients(queryDTO);
        return ResponseEntity.ok(page);
    }

    /** 根据ID查询单个患者 */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        PatientDTO dto = patientManagementService.getPatientById(id);
        return ResponseEntity.ok(dto);
    }

    /** 修改患者：仅校验传值字段的格式，未传字段不校验 */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @Validated(UpdateGroup.class) @RequestBody PatientDTO patientDTO) {
        PatientDTO result = patientManagementService.updatePatient(id, patientDTO);
        return ResponseEntity.ok(result);
    }

    /** 逻辑删除患者 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientManagementService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}