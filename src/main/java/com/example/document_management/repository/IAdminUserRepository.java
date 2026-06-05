package com.example.document_management.repository;

import com.example.document_management.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IAdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByEmail(String email);
    Optional<AdminUser> findByResetToken(String resetToken);
}



