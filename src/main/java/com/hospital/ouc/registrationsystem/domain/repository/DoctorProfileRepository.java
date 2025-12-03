
package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    /**
     * 通过统一用户ID查询医生档案，用于登录后绑定身份。
     */
    Optional<DoctorProfile> findByUserId(Long userId);
}
