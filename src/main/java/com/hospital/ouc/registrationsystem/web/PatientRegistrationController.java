package com.hospital.ouc.registrationsystem.web;

import com.hospital.ouc.registrationsystem.domain.service.PatientRegistrationService;
import com.hospital.ouc.registrationsystem.web.dto.RegistrationRequestDTO;
import com.hospital.ouc.registrationsystem.web.dto.RegistrationResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registration")
public class PatientRegistrationController {

    private final PatientRegistrationService registrationService;

    public PatientRegistrationController(PatientRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponseDTO> register(@RequestBody RegistrationRequestDTO request) {
        RegistrationResponseDTO resp = registrationService.register(request);
        return ResponseEntity.ok(resp);
    }
}
