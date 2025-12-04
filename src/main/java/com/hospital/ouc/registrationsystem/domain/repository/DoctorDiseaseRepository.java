package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorDisease;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoctorDiseaseRepository extends JpaRepository<DoctorDisease, Long> {
    // 根据疾病ID查找可以治疗该疾病的医生
    List<DoctorDisease> findByDiseaseId(Long diseaseId);

    // 根据医生ID查找其可治疗的疾病
    List<DoctorDisease> findByDoctorProfileId(Long doctorId);

    // 检查医生是否能够治疗某种疾病
    boolean existsByDoctorProfileIdAndDiseaseId(Long doctorId, Long diseaseId);
}