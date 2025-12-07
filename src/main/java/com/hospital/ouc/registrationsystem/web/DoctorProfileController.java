package com.hospital.ouc.registrationsystem.web;

import com.hospital.ouc.registrationsystem.domain.entity.DoctorDisease;
import com.hospital.ouc.registrationsystem.domain.repository.AppUserRepository;
import com.hospital.ouc.registrationsystem.domain.repository.DoctorDiseaseRepository;
import com.hospital.ouc.registrationsystem.domain.repository.DoctorProfileRepository;
import com.hospital.ouc.registrationsystem.domain.service.Admin_DoctorService;
import com.hospital.ouc.registrationsystem.domain.service.BusinessException;
import com.hospital.ouc.registrationsystem.domain.service.DutyScheduleService;
import com.hospital.ouc.registrationsystem.domain.entity.DoctorDutySchedule;
import com.hospital.ouc.registrationsystem.web.dto.ChangePasswordRequest;
import com.hospital.ouc.registrationsystem.web.dto.DoctorDTO;
import com.hospital.ouc.registrationsystem.web.dto.DoctorSelfDTO;
import com.hospital.ouc.registrationsystem.web.dto.DoctorUpdateDTO;
import com.hospital.ouc.registrationsystem.web.dto.DiseaseDTO;
import com.hospital.ouc.registrationsystem.web.dto.DoctorDutySimpleDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor/profile")
@RequiredArgsConstructor
public class DoctorProfileController {

    private final Admin_DoctorService doctorService;
    private final AppUserRepository appUserRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorDiseaseRepository doctorDiseaseRepository;
    private final DutyScheduleService dutyScheduleService;

    private static final String SALT = "OucWebDev123";

    @GetMapping("/{id}")
    public ResponseEntity<DoctorSelfDTO> getProfile(@PathVariable Long id) {
        DoctorDTO dto = doctorService.getDoctorById(id);
        DoctorSelfDTO self = convertToSelfDTO(dto);
        return ResponseEntity.ok(self);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<DoctorSelfDTO> getProfileByUserId(@PathVariable Long userId) {
        var profileOpt = doctorProfileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) throw new BusinessException("医生档案不存在");
        Long profileId = profileOpt.get().getId();
        DoctorDTO dto = doctorService.getDoctorById(profileId);
        DoctorSelfDTO self = convertToSelfDTO(dto);
        return ResponseEntity.ok(self);
    }

    /** 修改医生信息（仅允许修改用户名） */
    @PutMapping("/{id}")
    public ResponseEntity<DoctorSelfDTO> updateProfile(@PathVariable Long id,
                                                   @RequestBody DoctorUpdateDTO updateDTO) {
        // Only allow username updates from doctor self-service
        DoctorUpdateDTO safe = new DoctorUpdateDTO();
        safe.setUsername(updateDTO.getUsername());
        DoctorDTO result = doctorService.updateDoctor(id, safe);
        DoctorSelfDTO self = convertToSelfDTO(result);
        return ResponseEntity.ok(self);
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                               @Valid @RequestBody ChangePasswordRequest req,
                                               HttpServletRequest request) {
        // id may be doctorProfileId or userId; prefer profile id then fallback
        DoctorDTO profile;
        try {
            profile = doctorService.getDoctorById(id);
        } catch (RuntimeException ex) {
            var profileOpt = doctorProfileRepository.findByUserId(id);
            if (profileOpt.isEmpty()) throw ex;
            profile = doctorService.getDoctorById(profileOpt.get().getId());
        }

        Long profileId = profile.getId();
        var profileEntityOpt = doctorProfileRepository.findById(profileId);
        if (profileEntityOpt.isEmpty()) throw new BusinessException("医生档案不存在");
        var user = profileEntityOpt.get().getUser();
        if (user == null) throw new BusinessException("关联用户不存在");

        String hashed = sha256Hex(req.getNewPassword() + SALT);
        user.setPassword(hashed);
        appUserRepository.save(user);

        try {
            var session = request.getSession(false);
            if (session != null) session.invalidate();
        } catch (Exception ignored) {
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/duties/weekend")
    public ResponseEntity<List<DoctorDutySimpleDTO>> getDoctorWeekendDuties(@PathVariable Long id) {
        List<DoctorDutySchedule> schedules = dutyScheduleService.getDutySchedulesByDoctorId(id);
        List<DoctorDutySimpleDTO> list = schedules.stream().map(d -> {
            DoctorDutySimpleDTO dto = new DoctorDutySimpleDTO();
            dto.setId(d.getId());
            dto.setWeekendType(d.getWeekendType());
            dto.setDutyTimeslot(d.getDutyTimeslot());
            if (d.getDepartment() != null) {
                dto.setDepartmentId(d.getDepartment().getId());
                dto.setDepartmentName(d.getDepartment().getDepartmentName());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    private DoctorSelfDTO convertToSelfDTO(DoctorDTO dto) {
        DoctorSelfDTO self = new DoctorSelfDTO();
        self.setId(dto.getId());
        self.setUsername(dto.getUsername());
        self.setDoctorId(dto.getDoctorId());
        self.setName(dto.getName());
        self.setAge(dto.getAge());
        self.setGender(dto.getGender());
        self.setTitle(dto.getTitle());
        self.setDepartmentId(dto.getDepartmentId());
        self.setDepartmentName(dto.getDepartmentName());

        // load diseases with names
        List<DoctorDisease> dds = doctorDiseaseRepository.findByDoctorProfileId(dto.getId());
        List<DiseaseDTO> diseaseDTOs = dds.stream().map(dd -> {
            var d = dd.getDisease();
            DiseaseDTO di = new DiseaseDTO();
            di.setId(d.getId());
            di.setName(d.getName());
            di.setCode(d.getCode());
            di.setDescription(d.getDescription());
            if (d.getDepartment() != null) {
                di.setDepartmentId(d.getDepartment().getId());
                di.setDepartmentName(d.getDepartment().getDepartmentName());
            }
            return di;
        }).collect(Collectors.toList());
        self.setDiseases(diseaseDTOs);
        return self;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}
