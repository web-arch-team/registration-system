
package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    /**
     * 通过统一用户ID查询医生档案，用于登录后绑定身份。
     */
    Optional<DoctorProfile> findByUserId(Long userId);

    // 根据科室ID查找医生
    List<DoctorProfile> findByDepartmentId(Long departmentId);

    // 查找所有有效的医生
    List<DoctorProfile> findByIsActiveTrue();

    // 根据科室ID查找有效的医生
    List<DoctorProfile> findByDepartmentIdAndIsActiveTrue(Long departmentId);
}
