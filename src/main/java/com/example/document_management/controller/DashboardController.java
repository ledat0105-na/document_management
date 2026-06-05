package com.example.document_management.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.document_management.service.impl.DocumentServiceImpl;
import com.example.document_management.service.ICategoryService;
import com.example.document_management.service.AdminUserService;
import com.example.document_management.model.AdminUser;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DocumentServiceImpl docService;
    private final ICategoryService categoryService;
    private final AdminUserService adminUserService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : "User";
        model.addAttribute("userEmail", email);
        adminUserService.findByEmail(email).ifPresent(u -> model.addAttribute("displayName", u.getDisplayName() != null ? u.getDisplayName() : email));
        
        long totalDocs = docService.countTotalDocuments(false);
        long archivedDocs = docService.countTotalDocuments(true);
        int folderCount = categoryService.listAllCategories().size();
        
        model.addAttribute("totalDocs", totalDocs);
        model.addAttribute("archivedDocs", archivedDocs);
        model.addAttribute("folderCount", folderCount);
        
        return "dashboard/index";
    }

    @GetMapping("/dashboard/guide")
    public String guide() {
        return "dashboard/guide";
    }

    @GetMapping("/dashboard/check-data")
    @ResponseBody
    public Map<String, Object> checkData() {
        Map<String, Object> response = new HashMap<>();
        
        long totalDocs = docService.countTotalDocuments(false);
        long archivedDocs = docService.countTotalDocuments(true);
        int folderCount = categoryService.listAllCategories().size();
        
        boolean hasData = totalDocs > 0 || archivedDocs > 0 || folderCount > 0;
        
        response.put("hasData", hasData);
        response.put("documents", totalDocs);
        response.put("folders", folderCount);
        response.put("archived", archivedDocs);
        
        return response;
    }

    @PostMapping("/dashboard/delete-account")
    @ResponseBody
    public Map<String, Object> deleteAccount() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get current user email
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth != null ? auth.getName() : null;
            
            if (email == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin người dùng");
                return response;
            }
            
            // Check if there are other accounts
            long totalAccounts = adminUserService.countUsers();
            if (totalAccounts <= 1) {
                response.put("success", false);
                response.put("message", "Không thể xóa tài khoản cuối cùng");
                return response;
            }
            
            // Find and delete the current user's account
            Optional<AdminUser> userOpt = adminUserService.findByEmail(email);
            if (userOpt.isPresent()) {
                adminUserService.deleteById(userOpt.get().getId());
                response.put("success", true);
                response.put("message", "Đã xóa tài khoản thành công");
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy tài khoản");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xóa tài khoản: " + e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;
        if (email != null) {
            adminUserService.findByEmail(email).ifPresent(u -> {
                model.addAttribute("email", u.getEmail());
                model.addAttribute("displayName", u.getDisplayName());
            });
        }
        return "dashboard/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam(value = "displayName", required = false) String displayName,
                                @RequestParam(value = "email", required = false) String newEmail,
                                @RequestParam(value = "password", required = false) String password,
                                @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                RedirectAttributes ra) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth != null ? auth.getName() : null;
        if (currentEmail == null) {
            ra.addFlashAttribute("error", "Không xác định được người dùng");
            return "redirect:/profile";
        }
        try {
            var user = adminUserService.findByEmail(currentEmail).orElseThrow();
            if (password != null && !password.isBlank()) {
                if (!password.equals(confirmPassword)) {
                    ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
                    return "redirect:/profile";
                }
                String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";
                if (!password.matches(passwordRegex)) {
                    ra.addFlashAttribute("error", "Mật khẩu phải tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.");
                    return "redirect:/profile";
                }
            }
            if (newEmail != null && !newEmail.isBlank()) {
                String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
                if (!newEmail.matches(emailRegex)) {
                    ra.addFlashAttribute("error", "Email không hợp lệ.");
                    return "redirect:/profile";
                }
            }
            adminUserService.updateProfile(user, displayName, newEmail, password);
            ra.addFlashAttribute("message", "Cập nhật thành công");
            return "redirect:/dashboard";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }
}

