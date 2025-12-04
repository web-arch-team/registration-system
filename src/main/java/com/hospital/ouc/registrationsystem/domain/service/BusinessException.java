package com.hospital.ouc.registrationsystem.domain.service;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
