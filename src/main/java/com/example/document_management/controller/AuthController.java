package com.example.document_management.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.document_management.service.AdminUserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AdminUserService adminUserService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            return "redirect:/documents";
        }
        long userCount = adminUserService.countUsers();
        model.addAttribute("userCount", userCount);
        return "auth/login";
    }

    @GetMapping("/forgot")
    public String forgotPage() {
        return "auth/forgot";
    }

    @PostMapping("/forgot")
    public String handleForgot(@RequestParam("email") String email, Model model, HttpServletRequest request) {
        try {
            String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
            boolean sent = adminUserService.sendPasswordResetEmail(email, baseUrl);
            if (sent) {
                model.addAttribute("message", "Nếu email tồn tại, đường dẫn đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư.");
            } else {
                model.addAttribute("error", "Email không tồn tại trong hệ thống.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Không thể gửi email. Vui lòng thử lại sau.");
        }
        return "auth/forgot";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        long userCount = adminUserService.countUsers();
        model.addAttribute("userCount", userCount);
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam("email") String email,
                                 @RequestParam("password") String password,
                                 Model model) {
        try {
            // Basic email & strong password validation using regex
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"; // 8+ with upper/lower/digit/special

            if (email == null || !email.matches(emailRegex)) {
                model.addAttribute("error", "Email không hợp lệ.");
                model.addAttribute("userCount", adminUserService.countUsers());
                return "auth/register";
            }
            if (password == null || !password.matches(passwordRegex)) {
                model.addAttribute("error", "Mật khẩu phải tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.");
                model.addAttribute("userCount", adminUserService.countUsers());
                return "auth/register";
            }
            adminUserService.register(email, password);
            model.addAttribute("message", "Đăng ký thành công. Vui lòng đăng nhập.");
            return "auth/login";
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("userCount", adminUserService.countUsers());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("userCount", adminUserService.countUsers());
        } catch (Exception ex) {
            model.addAttribute("error", "Không thể đăng ký: " + ex.getMessage());
            model.addAttribute("userCount", adminUserService.countUsers());
        }
        return "auth/register";
    }

    @GetMapping("/reset")
    public String resetPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset";
    }

    @PostMapping("/reset")
    public String handleReset(@RequestParam("token") String token,
                              @RequestParam("password") String password,
                              @RequestParam("confirmPassword") String confirmPassword,
                              Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
            model.addAttribute("token", token);
            return "auth/reset";
        }
        
        if (password.length() < 6) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
            model.addAttribute("token", token);
            return "auth/reset";
        }
        
        boolean success = adminUserService.resetPassword(token, password);
        if (success) {
            model.addAttribute("message", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập.");
            return "auth/login";
        } else {
            model.addAttribute("error", "Liên kết không hợp lệ hoặc đã hết hạn.");
            return "auth/forgot";
        }
    }
}



