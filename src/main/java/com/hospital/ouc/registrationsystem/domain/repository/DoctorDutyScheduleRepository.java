package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorDutySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorDutyScheduleRepository extends JpaRepository<DoctorDutySchedule, Long> {
    // 根据科室ID查询值班记录
    List<DoctorDutySchedule> findByDepartmentId(Long departmentId);

    // 检查同一科室、同一周末类型、同一时段是否已存在值班记录
    boolean existsByDepartmentIdAndWeekendTypeAndDutyTimeslot(Long departmentId, Integer weekendType, String dutyTimeslot);

    // 根据科室ID批量删除
    void deleteByDepartmentId(Long departmentId);

    // 使用 JOIN FETCH 预先加载关联实体，避免在序列化时触发懒加载异常或返回空
    @Query("SELECT d FROM DoctorDutySchedule d LEFT JOIN FETCH d.department LEFT JOIN FETCH d.doctorProfile")
    List<DoctorDutySchedule> findAllWithJoins();

    @Query("SELECT d FROM DoctorDutySchedule d LEFT JOIN FETCH d.department LEFT JOIN FETCH d.doctorProfile WHERE d.department.id = :deptId")
    List<DoctorDutySchedule> findByDepartmentIdWithJoins(@Param("deptId") Long deptId);

    @Query("SELECT d FROM DoctorDutySchedule d LEFT JOIN FETCH d.department LEFT JOIN FETCH d.doctorProfile WHERE d.doctorProfile.id = :doctorId AND (d.weekendType = 6 OR d.weekendType = 7)")
    List<DoctorDutySchedule> findByDoctorProfileIdWithJoins(@Param("doctorId") Long doctorId);

    @Query("SELECT d FROM DoctorDutySchedule d LEFT JOIN FETCH d.department LEFT JOIN FETCH d.doctorProfile "
            + "WHERE (:deptId IS NULL OR d.department.id = :deptId) "
            + "AND (:weekendType IS NULL OR d.weekendType = :weekendType) "
            + "AND (:dutyTimeslot IS NULL OR d.dutyTimeslot = :dutyTimeslot)")
    List<DoctorDutySchedule> findByFiltersWithJoins(@Param("deptId") Long deptId,
                                                   @Param("weekendType") Integer weekendType,
                                                   @Param("dutyTimeslot") String dutyTimeslot);
}
