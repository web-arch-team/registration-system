package com.hospital.ouc.registrationsystem.config;

import com.hospital.ouc.registrationsystem.domain.repository.AppUserRepository;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 启动后初始化（当前不做任何处理）：
 * 数据库中密码为明文，故不做加密升级，保持与 init.sql 一致。
 */
@Component
public class DataInitializer {

    private final AppUserRepository appUserRepository;

    public DataInitializer(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @PostConstruct
    public void noop() {
        // 不进行任何密码处理，确保兼容数据库的明文密码
        // 如果未来切换为加密存储（BCrypt 等），再启用相应迁移逻辑
    }
}
