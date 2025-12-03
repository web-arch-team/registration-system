package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.PatientDoctorRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientDoctorRegistrationRepository extends JpaRepository<PatientDoctorRegistration, Long> {

    @Query("select r from PatientDoctorRegistration r " +
           "join r.doctorProfile d " +
           "where d.doctorId = :doctorId and r.weekday = :weekday")
    List<PatientDoctorRegistration> findByDoctorIdAndWeekday(@Param("doctorId") String doctorId,
                                                             @Param("weekday") Integer weekday);
}
