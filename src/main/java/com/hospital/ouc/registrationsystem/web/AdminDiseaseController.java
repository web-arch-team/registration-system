package com.hospital.ouc.registrationsystem.web;

import com.hospital.ouc.registrationsystem.domain.service.DiseaseService;
import com.hospital.ouc.registrationsystem.web.dto.DiseaseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/diseases")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDiseaseController {

    private final DiseaseService diseaseService;

    /**
     * 获取所有疾病
     */
    @GetMapping
    public ResponseEntity<List<DiseaseDTO>> getAllDiseases() {
        return ResponseEntity.ok(diseaseService.getAllDiseases());
    }

    /**
     * 根据ID获取疾病
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiseaseDTO> getDiseaseById(@PathVariable Long id) {
        return ResponseEntity.ok(diseaseService.getDiseaseById(id));
    }

    /**
     * 根据科室ID获取疾病
     */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DiseaseDTO>> getDiseasesByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(diseaseService.getDiseasesByDepartment(departmentId));
    }

    /**
     * 创建新疾病
     */
    @PostMapping
    public ResponseEntity<DiseaseDTO> createDisease(@Validated @RequestBody DiseaseDTO diseaseDTO) {
        DiseaseDTO created = diseaseService.createDisease(diseaseDTO);
        return ResponseEntity.status(201).body(created);
    }

    /**
     * 更新疾病信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiseaseDTO> updateDisease(
            @PathVariable Long id,
            @Validated @RequestBody DiseaseDTO diseaseDTO) {
        return ResponseEntity.ok(diseaseService.updateDisease(id, diseaseDTO));
    }

    /**
     * 删除疾病
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisease(@PathVariable Long id) {
        diseaseService.deleteDisease(id);
        return ResponseEntity.noContent().build();
    }
}