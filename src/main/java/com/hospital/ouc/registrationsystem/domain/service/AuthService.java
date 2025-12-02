package com.hospital.ouc.registrationsystem.domain.service;

import com.hospital.ouc.registrationsystem.domain.entity.AppUser;
import com.hospital.ouc.registrationsystem.domain.repository.AppUserRepository;
import com.hospital.ouc.registrationsystem.web.dto.LoginRequest;
import com.hospital.ouc.registrationsystem.web.dto.LoginResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务：处理最基本的用户名+密码登录。
 */
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 登录逻辑：
     * 1. 根据用户名查询用户
     * 2. 校验密码（支持已被 DataInitializer 加密后的密码）
     * 3. 返回响应 DTO
     */
    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole().name());
        return resp;
    }
}

