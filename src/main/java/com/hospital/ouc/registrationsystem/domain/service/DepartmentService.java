package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.Department;
import com.hospital.ouc.registrationsystem.domain.entity.Disease;
import com.hospital.ouc.registrationsystem.domain.repository.DepartmentRepository;
import com.hospital.ouc.registrationsystem.domain.repository.DiseaseRepository;
import com.hospital.ouc.registrationsystem.web.dto.DepartmentDTO;
import com.hospital.ouc.registrationsystem.web.dto.DiseaseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DiseaseRepository diseaseRepository;

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("科室不存在: " + id));
        return convertToDTO(department);
    }

    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        // 检查科室名称是否已存在
        if (departmentRepository.existsByDepartmentName(dto.getDepartmentName())) {
            throw new IllegalArgumentException("科室名称已存在: " + dto.getDepartmentName());
        }

        Department department = Department.builder()
                .departmentName(dto.getDepartmentName())
                .build();

        Department saved = departmentRepository.save(department);

        // 如果前端传入了疾病列表，则在创建科室后批量插入疾病
        if (dto.getDiseases() != null && !dto.getDiseases().isEmpty()) {
            List<Disease> toSave = dto.getDiseases().stream().map(d -> {
                Disease disease = Disease.builder()
                        .name(d.getName())
                        .code(d.getCode())
                        .description(d.getDescription())
                        .department(saved)
                        .build();
                return disease;
            }).collect(Collectors.toList());
            diseaseRepository.saveAll(toSave);
        }

        // 返回包含疾病列表的 DTO
        return convertToDTO(departmentRepository.findById(saved.getId()).orElse(saved));
    }

    @Transactional
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("科室不存在: " + id));

        // 检查名称是否已被其他科室使用
        if (!department.getDepartmentName().equals(dto.getDepartmentName()) &&
                departmentRepository.existsByDepartmentName(dto.getDepartmentName())) {
            throw new IllegalArgumentException("科室名称已存在: " + dto.getDepartmentName());
        }

        department.setDepartmentName(dto.getDepartmentName());
        Department updated = departmentRepository.save(department);

        // 可选：如果前端提供 diseases，则更新该科室的疾病集合（简单策略：删除旧的再插入新的）
        if (dto.getDiseases() != null) {
            // 先删除该科室所有疾病
            List<Disease> existing = diseaseRepository.findByDepartmentId(id);
            if (!existing.isEmpty()) {
                diseaseRepository.deleteAll(existing);
                diseaseRepository.flush();
            }
            // 再插入新的疾病
            List<Disease> toSave = dto.getDiseases().stream().map(d -> {
                Disease disease = Disease.builder()
                        .name(d.getName())
                        .code(d.getCode())
                        .description(d.getDescription())
                        .department(updated)
                        .build();
                return disease;
            }).collect(Collectors.toList());
            if (!toSave.isEmpty()) diseaseRepository.saveAll(toSave);
        }

        return convertToDTO(updated);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        // 检查是否有关联的疾病
        List<Disease> diseases = diseaseRepository.findByDepartmentId(id);
        if (!diseases.isEmpty()) {
            throw new IllegalStateException("该科室下存在疾病，无法删除");
        }

        departmentRepository.deleteById(id);
    }

    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setDepartmentName(department.getDepartmentName());

        // 填充该科室的疾病列表
        List<Disease> diseases = diseaseRepository.findByDepartmentId(department.getId());
        List<DiseaseDTO> diseaseDTOs = diseases.stream().map(d -> {
            DiseaseDTO dd = new DiseaseDTO();
            dd.setId(d.getId());
            dd.setName(d.getName());
            dd.setCode(d.getCode());
            dd.setDescription(d.getDescription());
            dd.setDepartmentId(d.getDepartment().getId());
            dd.setDepartmentName(d.getDepartment().getDepartmentName());
            return dd;
        }).collect(Collectors.toList());
        dto.setDiseases(diseaseDTOs);

        return dto;
    }
}