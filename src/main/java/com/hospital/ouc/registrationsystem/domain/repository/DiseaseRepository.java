package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.Disease;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    // 根据科室ID查找疾病
    List<Disease> findByDepartmentId(Long departmentId);

    // 根据疾病名称模糊查询
    List<Disease> findByNameContaining(String name);
}