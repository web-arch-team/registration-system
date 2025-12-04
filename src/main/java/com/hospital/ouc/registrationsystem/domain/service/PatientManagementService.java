package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.web.dto.PatientDTO;
import com.hospital.ouc.registrationsystem.web.dto.PatientQueryDTO;
import org.springframework.data.domain.Page;

public interface PatientManagementService {
    /**
     * 新增患者
     */
    PatientDTO addPatient(PatientDTO patientDTO);

    /**
     * 分页查询患者
     */
    Page<PatientDTO> queryPatients(PatientQueryDTO queryDTO);

    /**
     * 根据ID查询患者
     */
    PatientDTO getPatientById(Long id);

    /**
     * 修改患者信息
     */
    PatientDTO updatePatient(Long id, PatientDTO patientDTO);

    /**
     * 逻辑删除患者（设置is_active=false）
     */
    void deletePatient(Long id);
}
