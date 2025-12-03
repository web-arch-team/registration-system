package com.hospital.ouc.registrationsystem.web;

import com.hospital.ouc.registrationsystem.domain.service.AuthService;
import com.hospital.ouc.registrationsystem.web.dto.LoginRequest;
import com.hospital.ouc.registrationsystem.web.dto.LoginResponse;
import com.hospital.ouc.registrationsystem.web.dto.RegisterPatientRequest;
import com.hospital.ouc.registrationsystem.web.dto.RegisterPatientResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 登录接口控制器。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse resp = authService.login(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterPatientResponse> register(@RequestBody RegisterPatientRequest request) {
        RegisterPatientResponse resp = authService.registerPatient(request);
        return ResponseEntity.ok(resp);
    }

    /**
     * 一个极简的角色测试接口（无需鉴权，仅演示）。
     */
    @GetMapping("/echo")
    public ResponseEntity<String> echo() {
        return ResponseEntity.ok("auth service alive");
    }
}
