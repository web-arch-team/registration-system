package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.*;
import com.hospital.ouc.registrationsystem.domain.enums.Gender;
import com.hospital.ouc.registrationsystem.domain.enums.Role;
import com.hospital.ouc.registrationsystem.domain.repository.*;
import com.hospital.ouc.registrationsystem.web.dto.DoctorDTO;
import com.hospital.ouc.registrationsystem.web.dto.DoctorUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Admin_DoctorService {
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final DiseaseRepository diseaseRepository;
    private final DoctorDiseaseRepository doctorDiseaseRepository;

    // 固定盐值，与系统保持一致
    private static final String SALT = "OucWebDev123";

    // 获取所有医生
    public List<DoctorDTO> getAllDoctors() {
        return doctorProfileRepository.findByIsActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 根据ID获取医生
    public DoctorDTO getDoctorById(Long id) {
        DoctorProfile doctorProfile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        return convertToDTO(doctorProfile);
    }

    // 创建新医生
    @Transactional
    public DoctorDTO createDoctor(DoctorDTO doctorDTO) {
        // 检查用户名是否已存在
        if (appUserRepository.findByUsername(doctorDTO.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查医生工号是否已存在
        if (doctorProfileRepository.findByDoctorId(doctorDTO.getDoctorId()).isPresent()) {
            throw new RuntimeException("医生工号已存在");
        }

        // 创建用户账号
        AppUser appUser = AppUser.builder()
                .username(doctorDTO.getUsername())
                .password(sha256Hex(SALT + doctorDTO.getPassword()))
                .role(Role.DOCTOR)
                .isActive(true)
                .build();
        appUser = appUserRepository.save(appUser);

        // 获取科室
        Department department = departmentRepository.findById(doctorDTO.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("科室不存在"));

        // 创建医生档案
        DoctorProfile doctorProfile = DoctorProfile.builder()
                .user(appUser)
                .doctorId(doctorDTO.getDoctorId())
                .name(doctorDTO.getName())
                .age(doctorDTO.getAge())
                .gender(Gender.valueOf(doctorDTO.getGender().toLowerCase()))
                .title(doctorDTO.getTitle())
                .department(department)
                .isActive(true)
                .build();
        doctorProfile = doctorProfileRepository.save(doctorProfile);

        // 关联疾病
        if (doctorDTO.getDiseaseIds() != null && !doctorDTO.getDiseaseIds().isEmpty()) {
            saveDoctorDiseases(doctorProfile.getId(), doctorDTO.getDiseaseIds());
        }

        return convertToDTO(doctorProfile);
    }

    // 更新医生信息
    // Admin_DoctorService.java 中的updateDoctor方法
    @Transactional
    public DoctorDTO updateDoctor(Long id, DoctorUpdateDTO updateDTO) {
        DoctorProfile doctorProfile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        AppUser appUser = doctorProfile.getUser();

        // 更新用户表信息
        if (updateDTO.getUsername() != null) {
            if (appUserRepository.findByUsername(updateDTO.getUsername())
                    .filter(u -> !u.getId().equals(appUser.getId()))
                    .isPresent()) {
                throw new RuntimeException("用户名已存在");
            }
            appUser.setUsername(updateDTO.getUsername());
        }

        if (updateDTO.getPassword() != null) {
            appUser.setPassword(sha256Hex(SALT + updateDTO.getPassword()));
        }

        // 更新医生档案信息
        if (updateDTO.getDoctorId() != null) {
            if (doctorProfileRepository.findByDoctorId(updateDTO.getDoctorId())
                    .filter(d -> !d.getId().equals(id))
                    .isPresent()) {
                throw new RuntimeException("医生工号已存在");
            }
            doctorProfile.setDoctorId(updateDTO.getDoctorId());
        }

        if (updateDTO.getName() != null) {
            doctorProfile.setName(updateDTO.getName());
        }

        if (updateDTO.getAge() != null) {
            doctorProfile.setAge(updateDTO.getAge());
        }

        if (updateDTO.getGender() != null) {
            doctorProfile.setGender(Gender.valueOf(updateDTO.getGender().toLowerCase()));
        }

        if (updateDTO.getTitle() != null) {
            doctorProfile.setTitle(updateDTO.getTitle());
        }

        if (updateDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(updateDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("科室不存在"));
            doctorProfile.setDepartment(department);
        }

        if (updateDTO.getIsActive() != null) {
            doctorProfile.setIsActive(updateDTO.getIsActive());
            appUser.setIsActive(updateDTO.getIsActive());
        }

        // 保存用户信息
        appUserRepository.save(appUser);
        // 保存医生档案信息
        doctorProfile = doctorProfileRepository.save(doctorProfile);

        // 更新疾病关联
        if (updateDTO.getDiseaseIds() != null) {
            // 1. 先删除该医生所有已有的疾病关联
            doctorDiseaseRepository.deleteByDoctorProfileId(id);
            // 2. 确保删除操作立即执行（有些情况下JPA可能会延迟执行）
            doctorDiseaseRepository.flush();
            // 3. 再插入新的疾病关联
            saveDoctorDiseases(id, updateDTO.getDiseaseIds());
        }

        return convertToDTO(doctorProfile);
    }

    // 软删除医生（设置is_active为false）
    @Transactional
    public void deleteDoctor(Long id) {
        DoctorProfile doctorProfile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("医生不存在"));

        // 软删除医生档案
        doctorProfile.setIsActive(false);
        doctorProfileRepository.save(doctorProfile);

        // 同时软删除关联的用户
        AppUser appUser = doctorProfile.getUser();
        appUser.setIsActive(false);
        appUserRepository.save(appUser);
    }

    // 保存医生与疾病的关联关系
    private void saveDoctorDiseases(Long doctorProfileId, List<Long> diseaseIds) {
        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new RuntimeException("医生不存在"));

        List<DoctorDisease> doctorDiseases = diseaseIds.stream()
                .map(diseaseId -> {
                    Disease disease = diseaseRepository.findById(diseaseId)
                            .orElseThrow(() -> new RuntimeException("疾病不存在: " + diseaseId));
                    return DoctorDisease.builder()
                            .doctorProfile(doctorProfile)
                            .disease(disease)
                            .build();
                })
                .collect(Collectors.toList());

        doctorDiseaseRepository.saveAll(doctorDiseases);
    }

    // 转换实体到DTO
    private DoctorDTO convertToDTO(DoctorProfile doctorProfile) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctorProfile.getId());
        dto.setUsername(doctorProfile.getUser().getUsername());
        dto.setDoctorId(doctorProfile.getDoctorId());
        dto.setName(doctorProfile.getName());
        dto.setAge(doctorProfile.getAge());
        dto.setGender(doctorProfile.getGender().name());
        dto.setTitle(doctorProfile.getTitle());

        if (doctorProfile.getDepartment() != null) {
            dto.setDepartmentId(doctorProfile.getDepartment().getId());
            dto.setDepartmentName(doctorProfile.getDepartment().getDepartmentName());
        }

        dto.setActive(doctorProfile.getIsActive());

        // 设置关联的疾病ID
        List<Long> diseaseIds = doctorDiseaseRepository.findByDoctorProfileId(doctorProfile.getId()).stream()
                .map(dd -> dd.getDisease().getId())
                .collect(Collectors.toList());
        dto.setDiseaseIds(diseaseIds);

        return dto;
    }

    // SHA-256加密
    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}
