package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.AppUser;
import com.hospital.ouc.registrationsystem.domain.repository.AppUserRepository;
import com.hospital.ouc.registrationsystem.web.dto.LoginRequest;
import com.hospital.ouc.registrationsystem.web.dto.LoginResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 认证服务：处理最基本的用户名+密码登录。
 */
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;

    // 固定盐（与 init.sql 保持一致）
    private static final String SALT = "OucWebDev123";

    public AuthService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
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

        String inputHash = sha256Hex(SALT + (request.getPassword() == null ? "" : request.getPassword()));
        if (!inputHash.equalsIgnoreCase(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getId());
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
