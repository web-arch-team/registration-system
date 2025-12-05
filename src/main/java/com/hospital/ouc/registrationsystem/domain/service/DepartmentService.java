package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.Department;
import com.hospital.ouc.registrationsystem.domain.entity.Disease;
import com.hospital.ouc.registrationsystem.domain.repository.DepartmentRepository;
import com.hospital.ouc.registrationsystem.domain.repository.DiseaseRepository;
import com.hospital.ouc.registrationsystem.web.dto.DepartmentDTO;
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
        return convertToDTO(saved);
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
        return dto;
    }
}