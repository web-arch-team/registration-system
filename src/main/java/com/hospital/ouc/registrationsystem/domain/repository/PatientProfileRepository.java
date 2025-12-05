package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.PatientProfile;
import com.hospital.ouc.registrationsystem.domain.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
    //根据身份证号码查询
    Optional<PatientProfile> findByIdCard(String idCard);
    //根据手机号查询
    Optional<PatientProfile> findByPhoneNumber(String phoneNumber);
    //根据ID查询且必须是激活状态
    Optional<PatientProfile> findByIdAndIsActiveTrue(Long id);

    // 根据用户ID查询患者档案
    Optional<PatientProfile> findByUserId(Long userId);

    // 多条件分页查询
    @Query("SELECT p FROM PatientProfile p WHERE " +
            "(:#{#name} IS NULL OR p.name LIKE :#{#name}%) AND " +
            "(:#{#idCard} IS NULL OR p.idCard = :#{#idCard}) AND " +
            "(:#{#phoneNumber} IS NULL OR p.phoneNumber = :#{#phoneNumber}) AND " +
            "(:#{#gender} IS NULL OR p.gender = :#{#gender}) AND " +
            "p.isActive = true")
    Page<PatientProfile> findByCondition(
            @Param("name") String name,
            @Param("idCard") String idCard,
            @Param("phoneNumber") String phoneNumber,
            @Param("gender") Gender gender,
            Pageable pageable);
}
