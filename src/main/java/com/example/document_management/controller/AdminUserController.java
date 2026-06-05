package com.example.document_management.controller;

import com.example.document_management.repository.IAdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class AdminUserController {
    private final IAdminUserRepository repo;

    @GetMapping("/admin/users")
    public String list(Model model) {
        model.addAttribute("users", repo.findAll());
        model.addAttribute("count", repo.count());
        return "admin/users";
    }

    @PostMapping("/admin/users/delete-admin")
    public String deleteAdmin(Model model) {
        try {
            var adminUsers = repo.findAll();
            if (adminUsers.size() > 1) {
                var adminUser = adminUsers.stream()
                    .filter(u -> u.getEmail().equals("admin@example.com"))
                    .findFirst();
                if (adminUser.isPresent()) {
                    repo.delete(adminUser.get());
                    model.addAttribute("success", "Đã xóa tài khoản admin thành công");
                } else {
                    model.addAttribute("error", "Không tìm thấy tài khoản admin");
                }
            } else {
                model.addAttribute("error", "Không thể xóa tài khoản admin cuối cùng");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteById(@PathVariable Long id, Model model) {
        try {
            long count = repo.count();
            if (count <= 1) {
                model.addAttribute("error", "Cần giữ lại ít nhất 1 tài khoản để đăng nhập.");
            } else {
                repo.deleteById(id);
                model.addAttribute("success", "Đã xóa tài khoản thành công.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}



