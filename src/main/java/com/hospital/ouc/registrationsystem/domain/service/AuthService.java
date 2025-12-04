package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.AppUser;
import com.hospital.ouc.registrationsystem.domain.entity.DoctorProfile;
import com.hospital.ouc.registrationsystem.domain.entity.PatientProfile;
import com.hospital.ouc.registrationsystem.domain.enums.Gender;
import com.hospital.ouc.registrationsystem.domain.enums.Role;
import com.hospital.ouc.registrationsystem.domain.repository.AppUserRepository;
import com.hospital.ouc.registrationsystem.domain.repository.DoctorProfileRepository;
import com.hospital.ouc.registrationsystem.domain.repository.PatientProfileRepository;
import com.hospital.ouc.registrationsystem.web.dto.LoginRequest;
import com.hospital.ouc.registrationsystem.web.dto.LoginResponse;
import com.hospital.ouc.registrationsystem.web.dto.RegisterPatientRequest;
import com.hospital.ouc.registrationsystem.web.dto.RegisterPatientResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 认证服务：处理最基本的用户名+密码登录。
 */
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    // 固定盐（与 init.sql 保持一致）
    private static final String SALT = "OucWebDev123";

    public AuthService(AppUserRepository appUserRepository,
                       PatientProfileRepository patientProfileRepository,
                       DoctorProfileRepository doctorProfileRepository) {
        this.appUserRepository = appUserRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    /**
     * 登录逻辑：
     * 1. 根据用户名查询用户
     * 2. 对 (盐 + 输入密码) 做 SHA-256，转十六进制，与数据库中的哈希字符串比对
     * 3. 返回响应 DTO
     */
    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String plain = request.getPassword() == null ? "" : request.getPassword();
        // 与注册和 init.sql 保持一致：明文 + SALT -> sha256 hex
        String inputHash = sha256Hex(plain + SALT);
        if (!inputHash.equalsIgnoreCase(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole().name());
        // 绑定医生身份：登录的 DOCTOR 自动携带其 doctorId，前端无需再输入
        if (user.getRole() == Role.DOCTOR) {
            DoctorProfile doctor = doctorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("医生档案不存在"));
            resp.setDoctorId(doctor.getDoctorId());
        }
        return resp;
    }

    /**
     * 患者注册：创建 AppUser(PATIENT) + PatientProfile，并做基本唯一性校验。
     */
    @Transactional
    public RegisterPatientResponse registerPatient(RegisterPatientRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new RuntimeException("密码不能为空");
        }
        if (req.getIdCard() == null || req.getIdCard().isBlank()) {
            throw new RuntimeException("身份证不能为空");
        }
        if (req.getPhoneNumber() == null || req.getPhoneNumber().isBlank()) {
            throw new RuntimeException("手机号不能为空");
        }
        if (req.getGender() == null || req.getGender().isBlank()) {
            throw new RuntimeException("性别不能为空");
        }
        if (req.getName() == null || req.getName().isBlank()) {
            throw new RuntimeException("姓名不能为空");
        }

        // 用户名唯一
        appUserRepository.findByUsername(req.getUsername()).ifPresent(u -> {
            throw new RuntimeException("用户名已存在");
        });
        // 身份证唯一
        patientProfileRepository.findByIdCard(req.getIdCard()).ifPresent(pp -> {
            throw new RuntimeException("身份证已存在");
        });
        // 手机号唯一
        patientProfileRepository.findByPhoneNumber(req.getPhoneNumber()).ifPresent(pp -> {
            throw new RuntimeException("手机号已存在");
        });

        // 性别值规范化与校验
        String g = req.getGender().trim().toLowerCase();
        Gender gender;
        if ("male".equals(g)) {
            gender = Gender.male;
        } else if ("female".equals(g)) {
            gender = Gender.female;
        } else {
            throw new RuntimeException("性别取值不合法，应为 male 或 female");
        }

        // 生成密码哈希（与 init.sql 一致：明文 + SALT -> sha256 hex）
        String passwordHash = sha256Hex(req.getPassword() + SALT);

        // 保存 AppUser
        AppUser user = AppUser.builder()
                .username(req.getUsername())
                .password(passwordHash)
                .role(Role.PATIENT)
                .isActive(true)
                .build();
        user = appUserRepository.save(user);

        // 保存 PatientProfile
        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .idCard(req.getIdCard())
                .name(req.getName())
                .phoneNumber(req.getPhoneNumber())
                .age(req.getAge())
                .gender(gender)
                .isActive(true)
                .build();
        profile = patientProfileRepository.save(profile);

        RegisterPatientResponse resp = new RegisterPatientResponse();
        resp.setUserId(user.getId());
        resp.setPatientId(profile.getId());
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole().name());
        return resp;
    }

    /**
     * 计算 SHA-256 并返回十六进制字符串。
     */
    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
