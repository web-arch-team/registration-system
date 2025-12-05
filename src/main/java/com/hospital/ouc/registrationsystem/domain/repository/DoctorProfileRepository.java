package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long>, JpaSpecificationExecutor<DoctorProfile> {
    /**
     * 通过统一用户ID查询医生档案，用于登录后绑定身份。
     */
    Optional<DoctorProfile> findByUserId(Long userId);

    /**
     * 通过工号（doctorId）查询医生档案，用于 admin 管理。
     */
    Optional<DoctorProfile> findByDoctorId(String doctorId);

    // 查找所有医生（不区分 is_active 状态）—— admin 场景
    List<DoctorProfile> findAll();

    // 按 isActive 查询医生
    List<DoctorProfile> findByIsActive(boolean isActive);

    // 根据科室ID查找所有医生
    List<DoctorProfile> findByDepartmentId(Long departmentId);

    // 查找所有有效的医生
    List<DoctorProfile> findByIsActiveTrue();

    // 根据科室ID查找有效的医生
    List<DoctorProfile> findByDepartmentIdAndIsActiveTrue(Long departmentId);

    // 根据科室ID和激活状态查找医生（兼容旧逻辑）
    List<DoctorProfile> findByDepartmentIdAndIsActive(Long departmentId, boolean isActive);
}
