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
     * 2. 明文比对密码（与 init.sql 中一致）；若未来启用加密，则兼容 passwordEncoder.matches
     * 3. 返回响应 DTO
     */
    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 明文比对，保持与数据库中明文密码一致
        boolean match = request.getPassword() != null && request.getPassword().equals(user.getPassword());
        // 兼容：若未来某些用户密码是加密存储，这里仍然允许通过 encoder 校验
        if (!match && passwordEncoder != null) {
            try {
                match = passwordEncoder.matches(request.getPassword(), user.getPassword());
            } catch (Exception ignored) {
                // 忽略 encoder 不可用/异常，保持明文比对为主
            }
        }
        if (!match) {
            throw new RuntimeException("用户名或密码错误");
        }

        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole().name());
        return resp;
    }
}
