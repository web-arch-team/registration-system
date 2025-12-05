package com.hospital.ouc.registrationsystem.web;

import com.hospital.ouc.registrationsystem.domain.service.Admin_DoctorService;
import com.hospital.ouc.registrationsystem.web.dto.DoctorDTO;
import com.hospital.ouc.registrationsystem.web.dto.DoctorUpdateDTO;
import com.hospital.ouc.registrationsystem.web.dto.DoctorSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/doctors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问
public class AdminDoctorController {

    private final Admin_DoctorService doctorService;

    // 获取/查询医生（支持分页与筛选）
    @GetMapping
    public ResponseEntity<Page<DoctorDTO>> searchDoctors(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String doctorId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        DoctorSearchCriteria criteria = new DoctorSearchCriteria();
        criteria.setId(id);
        criteria.setDoctorId(doctorId);
        criteria.setName(name);
        criteria.setGender(gender);
        criteria.setTitle(title);
        criteria.setDepartmentId(departmentId);
        criteria.setDeleted(deleted);

        // parse sort like "id,desc" or "name,asc"
        String[] sortParts = sort.split(",");
        Sort s = Sort.by(Sort.Direction.fromString(sortParts.length > 1 ? sortParts[1] : "desc"), sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, s);

        return ResponseEntity.ok(doctorService.searchDoctors(criteria, pageable));
    }

    // 根据ID获取医生
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    // 创建新医生
    @PostMapping
    public ResponseEntity<DoctorDTO> createDoctor(@Validated @RequestBody DoctorDTO doctorDTO) {
        return ResponseEntity.ok(doctorService.createDoctor(doctorDTO));
    }

    // 更新医生信息
    @PutMapping("/{id}")
    public ResponseEntity<DoctorDTO> updateDoctor(
            @PathVariable Long id,
            @RequestBody DoctorUpdateDTO updateDTO) {  // 移除@Validated注解，允许部分字段更新
        return ResponseEntity.ok(doctorService.updateDoctor(id, updateDTO));
    }

    // 软删除医生
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
