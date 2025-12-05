package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.Department;
import com.hospital.ouc.registrationsystem.domain.entity.Disease;
import com.hospital.ouc.registrationsystem.domain.repository.DepartmentRepository;
import com.hospital.ouc.registrationsystem.domain.repository.DiseaseRepository;
import com.hospital.ouc.registrationsystem.web.dto.DiseaseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;
    private final DepartmentRepository departmentRepository;

    public List<DiseaseDTO> getAllDiseases() {
        return diseaseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DiseaseDTO getDiseaseById(Long id) {
        Disease disease = diseaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("疾病不存在: " + id));
        return convertToDTO(disease);
    }

    public List<DiseaseDTO> getDiseasesByDepartment(Long departmentId) {
        return diseaseRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DiseaseDTO createDisease(DiseaseDTO dto) {
        // 检查疾病代码是否已存在
        if (dto.getCode() != null && diseaseRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("疾病代码已存在: " + dto.getCode());
        }

        // 验证科室是否存在
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("科室不存在: " + dto.getDepartmentId()));

        Disease disease = Disease.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .department(department)
                .build();

        Disease saved = diseaseRepository.save(disease);
        return convertToDTO(saved);
    }

    @Transactional
    public DiseaseDTO updateDisease(Long id, DiseaseDTO dto) {
        Disease disease = diseaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("疾病不存在: " + id));

        // 仅在DTO中存在code且与原有值不同时才校验并更新
        if (dto.getCode() != null && !dto.getCode().equals(disease.getCode())) {
            if (diseaseRepository.existsByCode(dto.getCode())) {
                throw new IllegalArgumentException("疾病代码已存在: " + dto.getCode());
            }
            disease.setCode(dto.getCode());
        }

        // 仅在DTO中存在departmentId且与原有值不同时才校验并更新
        if (dto.getDepartmentId() != null && !dto.getDepartmentId().equals(disease.getDepartment().getId())) {
            Department newDepartment = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("科室不存在: " + dto.getDepartmentId()));
            disease.setDepartment(newDepartment);
        }

        // 仅在DTO中存在name时才更新
        if (dto.getName() != null) {
            disease.setName(dto.getName());
        }

        // 仅在DTO中存在description时才更新
        if (dto.getDescription() != null) {
            disease.setDescription(dto.getDescription());
        }

        Disease updated = diseaseRepository.save(disease);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteDisease(Long id) {
        // 可以根据实际业务添加关联检查，如是否有医生关联该疾病等
        diseaseRepository.deleteById(id);
    }

    private DiseaseDTO convertToDTO(Disease disease) {
        DiseaseDTO dto = new DiseaseDTO();
        dto.setId(disease.getId());
        dto.setName(disease.getName());
        dto.setCode(disease.getCode());
        dto.setDescription(disease.getDescription());
        dto.setDepartmentId(disease.getDepartment().getId());
        dto.setDepartmentName(disease.getDepartment().getDepartmentName());
        return dto;
    }
}