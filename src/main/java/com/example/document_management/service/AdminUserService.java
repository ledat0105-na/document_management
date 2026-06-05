package com.example.document_management.service;

import com.example.document_management.model.AdminUser;
import com.example.document_management.repository.IAdminUserRepository;
import com.example.document_management.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService implements UserDetailsService {

    private final IAdminUserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser u = repo.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Not found"));
        return User.withUsername(u.getEmail()).password(u.getPasswordHash()).roles("ADMIN").build();
    }

    public synchronized AdminUser register(String email, String rawPassword) {
        long count = repo.count();
        if (count >= 2) throw new IllegalStateException("Đã đạt tối đa 2 tài khoản.");
        repo.findByEmail(email).ifPresent(e -> { throw new IllegalArgumentException("Email đã tồn tại"); });
        AdminUser u = new AdminUser();
        u.setEmail(email.trim());
        u.setDisplayName(email.trim());
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        return repo.save(u);
    }

    public synchronized void deleteById(Long id) {
        if (repo.count() <= 1) throw new IllegalStateException("Phải còn ít nhất 1 tài khoản để đăng nhập.");
        repo.deleteById(id);
    }

    public long countUsers() {
        return repo.count();
    }

    public Optional<AdminUser> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public AdminUser updateProfile(AdminUser user, String displayName, String email, String newPassword) {
        if (displayName != null && !displayName.isBlank()) {
            user.setDisplayName(displayName.trim());
        }
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(user.getEmail())) {
            repo.findByEmail(email).ifPresent(e -> { throw new IllegalArgumentException("Email đã tồn tại"); });
            user.setEmail(email.trim());
        }
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6) throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
            user.setPasswordHash(passwordEncoder.encode(newPassword));
        }
        return repo.save(user);
    }

    public boolean sendPasswordResetEmail(String email, String resetUrl) {
        Optional<AdminUser> userOpt = repo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false; 
        }
        
        AdminUser user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiresAt(Instant.now().plusSeconds(3600)); 
        repo.save(user);
        
        String subject = "Đặt lại mật khẩu DocManager";
        String resetLink = resetUrl + "/reset?token=" + token;
        String body = String.format("""
            <h2>Đặt lại mật khẩu DocManager</h2>
            <p>Xin chào,</p>
            <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản <strong>%s</strong>.</p>
            <p>Nhấp vào liên kết sau để đặt lại mật khẩu (hết hạn sau 1 giờ):</p>
            <p><a href="%s" style="background:#246bff;color:white;padding:10px 20px;text-decoration:none;border-radius:8px;display:inline-block">Đặt lại mật khẩu</a></p>
            <p>Hoặc copy liên kết này vào trình duyệt:</p>
            <p style="word-break:break-all;color:#666">%s</p>
            <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
            <hr>
            <p style="color:#999;font-size:12px">DocManager - Hệ thống quản lý tài liệu</p>
            """, email, resetLink, resetLink);
        
        try {
            mailService.sendHtml(email, subject, body);
            return true;
        } catch (Exception e) {
            
            user.setResetToken(null);
            user.setResetTokenExpiresAt(null);
            repo.save(user);
            return false;
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<AdminUser> userOpt = repo.findByResetToken(token);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        AdminUser user = userOpt.get();
        if (user.getResetTokenExpiresAt() == null || user.getResetTokenExpiresAt().isBefore(Instant.now())) {
            return false; 
        }
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        repo.save(user);
        return true;
    }
}



