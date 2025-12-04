// src/main/java/com/hospital/ouc/registrationsystem/domain/service/impl/PatientManagementServiceImpl.java
package com.hospital.ouc.registrationsystem.domain.service.impl;

import com.hospital.ouc.registrationsystem.web.dto.PatientDTO;
import com.hospital.ouc.registrationsystem.web.dto.PatientQueryDTO;
import com.hospital.ouc.registrationsystem.domain.entity.AppUser;
import com.hospital.ouc.registrationsystem.domain.entity.PatientProfile;
import com.hospital.ouc.registrationsystem.domain.enums.Role;
import com.hospital.ouc.registrationsystem.domain.service.BusinessException;
import com.hospital.ouc.registrationsystem.domain.repository.AppUserRepository;
import com.hospital.ouc.registrationsystem.domain.repository.PatientProfileRepository;
import com.hospital.ouc.registrationsystem.domain.service.PatientManagementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PatientManagementServiceImpl implements PatientManagementService {

    private final PatientProfileRepository patientProfileRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    /** 新增患者：强制校验所有必填字段，创建用户+档案 */
    @Override
    @Transactional
    public PatientDTO addPatient(PatientDTO patientDTO) {
        // 1. 校验用户名唯一性（仅未删除用户）
        if (patientDTO.getUsername() != null && appUserRepository.findByUsername(patientDTO.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }
        // 2. 校验身份证唯一性
        if (patientProfileRepository.findByIdCard(patientDTO.getIdCard()).isPresent()) {
            throw new BusinessException("身份证号已存在");
        }
        // 3. 校验手机号唯一性
        if (patientProfileRepository.findByPhoneNumber(patientDTO.getPhoneNumber()).isPresent()) {
            throw new BusinessException("手机号已存在");
        }

        // 4. 创建用户（默认密码：身份证后6位）
        AppUser appUser = new AppUser();
        appUser.setUsername(patientDTO.getUsername());
        String defaultPwd = patientDTO.getIdCard().substring(patientDTO.getIdCard().length() - 6);
        appUser.setPassword(passwordEncoder.encode(defaultPwd));
        appUser.setRole(Role.PATIENT);
        appUser.setIsActive(true);
        AppUser savedUser = appUserRepository.save(appUser);

        // 5. 创建患者档案
        PatientProfile profile = new PatientProfile();
        profile.setUser(savedUser);
        profile.setIdCard(patientDTO.getIdCard());
        profile.setName(patientDTO.getName());
        profile.setPhoneNumber(patientDTO.getPhoneNumber());
        profile.setAge(patientDTO.getAge());
        profile.setGender(patientDTO.getGender());
        profile.setIsActive(true);
        PatientProfile savedProfile = patientProfileRepository.save(profile);

        // 6. 转换为DTO返回
        return convertToDTO(savedProfile, savedUser);
    }

    /** 分页查询患者：多条件筛选+分页，仅查未删除患者 */
    @Override
    public Page<PatientDTO> queryPatients(PatientQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(queryDTO.getPageNum() - 1, queryDTO.getPageSize());
        Page<PatientProfile> profilePage = patientProfileRepository.findByCondition(
                queryDTO.getName(),
                queryDTO.getIdCard(),
                queryDTO.getPhoneNumber(),
                queryDTO.getGender(),
                pageable
        );
        return profilePage.map(profile -> convertToDTO(profile, profile.getUser()));
    }

    /** 根据ID查询患者：仅查未删除患者 */
    @Override
    public PatientDTO getPatientById(Long id) {
        PatientProfile profile = patientProfileRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException("患者不存在或已被删除"));
        AppUser user = profile.getUser();
        return convertToDTO(profile, user);
    }

    /** 修改患者：核心逻辑 - 传啥改啥，未传字段保留原值 */
    @Override
    @Transactional
    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        // 1. 查询患者（仅未删除患者可修改）
        PatientProfile profile = patientProfileRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException("患者不存在或已被删除"));
        AppUser user = profile.getUser();

        // 2. 修改用户名（传值才处理，未传跳过）
        if (StringUtils.hasText(patientDTO.getUsername()) && !user.getUsername().equals(patientDTO.getUsername())) {
            if (appUserRepository.findByUsername(patientDTO.getUsername()).isPresent()) {
                throw new BusinessException("用户名已存在，无法修改");
            }
            user.setUsername(patientDTO.getUsername());
            appUserRepository.save(user);
        }

        // 3. 修改身份证号（传值才处理，未传跳过）
        if (StringUtils.hasText(patientDTO.getIdCard()) && !profile.getIdCard().equals(patientDTO.getIdCard())) {
            // 查找是否有其他患者使用该身份证号（排除当前患者）
            patientProfileRepository.findByIdCard(patientDTO.getIdCard()).ifPresent(existingProfile -> {
                if (!existingProfile.getId().equals(profile.getId())) {
                    throw new BusinessException("身份证号已存在，无法修改");
                }
            });
            profile.setIdCard(patientDTO.getIdCard());
        }

        // 4. 修改手机号（传值才处理，未传跳过）
        if (StringUtils.hasText(patientDTO.getPhoneNumber()) && !profile.getPhoneNumber().equals(patientDTO.getPhoneNumber())) {
            patientProfileRepository.findByPhoneNumber(patientDTO.getPhoneNumber())
                    .filter(existingProfile -> !existingProfile.getId().equals(profile.getId()))
                    .ifPresent(existingProfile -> { throw new BusinessException("手机号已存在，无法修改");});
            profile.setPhoneNumber(patientDTO.getPhoneNumber());
        }

        // 5. 修改姓名（传值才处理，未传跳过）
        if (StringUtils.hasText(patientDTO.getName())) {
            profile.setName(patientDTO.getName());
        }

        // 6. 修改年龄（传值才处理，未传跳过）
        if (patientDTO.getAge() != null) {
            profile.setAge(patientDTO.getAge());
        }

        // 7. 修改性别（传值才处理，未传跳过）
        if (patientDTO.getGender() != null) {
            profile.setGender(patientDTO.getGender());
        }

        // 8. 保存修改（仅保存有变更的字段）
        PatientProfile updatedProfile = patientProfileRepository.save(profile);

        // 9. 返回修改后的完整信息
        return convertToDTO(updatedProfile, user);
    }

    /** 逻辑删除患者：仅修改is_active=false，不物理删除 */
    @Override
    @Transactional
    public void deletePatient(Long id) {
        PatientProfile profile = patientProfileRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException("患者不存在或已被删除"));
        profile.setIsActive(false);
        profile.getUser().setIsActive(false); // 关联用户也标记为删除
        patientProfileRepository.save(profile);
        appUserRepository.save(profile.getUser());
    }

    /** 实体转DTO：隐藏敏感字段，适配前端展示 */
    private PatientDTO convertToDTO(PatientProfile profile, AppUser user) {
        PatientDTO dto = new PatientDTO();
        dto.setId(profile.getId());
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setIdCard(profile.getIdCard());
        dto.setName(profile.getName());
        dto.setPhoneNumber(profile.getPhoneNumber());
        dto.setAge(profile.getAge());
        dto.setGender(profile.getGender());
        dto.setIsActive(profile.getIsActive());
        return dto;
    }
}