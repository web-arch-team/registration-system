package com.hospital.ouc.registrationsystem.domain.repository;

import com.hospital.ouc.registrationsystem.domain.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hospital.ouc.registrationsystem.domain.enums.Role;

import java.util.Optional;

/**
 * 用户仓库接口，提供按用户名查询能力。
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByIdAndRoleAndIsActiveTrue(Long id, Role role);
}

