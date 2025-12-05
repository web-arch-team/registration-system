package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorDisease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorDiseaseRepository extends JpaRepository<DoctorDisease, Long> {
    // 根据疾病ID找可以治疗该疾病的医生
    List<DoctorDisease> findByDiseaseId(Long diseaseId);

    // 根据医生档案ID找其可治疗的疾病
    List<DoctorDisease> findByDoctorProfileId(Long doctorProfileId);

    // 删除某个医生的全部疾病关联（用于 admin 侧重建关联）
    void deleteByDoctorProfileId(Long doctorProfileId);

    // 检查医生是否能够治疗某种疾病
    boolean existsByDoctorProfileIdAndDiseaseId(Long doctorProfileId, Long diseaseId);
}