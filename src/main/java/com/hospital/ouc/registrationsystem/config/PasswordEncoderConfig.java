package com.hospital.ouc.registrationsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码“加密”配置：使用 NoOpPasswordEncoder（不做任何处理），
 * 以便兼容数据库中的明文密码。
 * 注意：仅用于开发演示环境，生产环境必须使用强哈希算法（如 BCrypt）。
 */
@Configuration
public class PasswordEncoderConfig {
    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
