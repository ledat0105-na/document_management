package com.example.document_management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorCode", "404");
                model.addAttribute("errorTitle", "Trang không tìm thấy");
                model.addAttribute("errorMessage", "Xin lỗi, trang bạn đang tìm kiếm không tồn tại.");
                model.addAttribute("suggestion", "Vui lòng kiểm tra lại đường dẫn hoặc quay về trang chủ.");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorCode", "403");
                model.addAttribute("errorTitle", "Không có quyền truy cập");
                model.addAttribute("errorMessage", "Bạn không có quyền truy cập vào trang này.");
                model.addAttribute("suggestion", "Vui lòng đăng nhập với tài khoản có quyền phù hợp.");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorCode", "500");
                model.addAttribute("errorTitle", "Lỗi máy chủ");
                model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình xử lý yêu cầu.");
                model.addAttribute("suggestion", "Vui lòng thử lại sau hoặc liên hệ quản trị viên.");
            } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                model.addAttribute("errorCode", "400");
                model.addAttribute("errorTitle", "Yêu cầu không hợp lệ");
                model.addAttribute("errorMessage", "Yêu cầu của bạn không đúng định dạng.");
                model.addAttribute("suggestion", "Vui lòng kiểm tra lại thông tin và thử lại.");
            } else {
                model.addAttribute("errorCode", statusCode.toString());
                model.addAttribute("errorTitle", "Đã xảy ra lỗi");
                model.addAttribute("errorMessage", "Có lỗi không xác định đã xảy ra.");
                model.addAttribute("suggestion", "Vui lòng thử lại sau.");
            }
        } else {
            model.addAttribute("errorCode", "Unknown");
            model.addAttribute("errorTitle", "Lỗi không xác định");
            model.addAttribute("errorMessage", "Đã xảy ra lỗi không xác định.");
            model.addAttribute("suggestion", "Vui lòng thử lại sau.");
        }
        
        return "error";
    }
    
    @RequestMapping("/maintenance")
    public String maintenance(Model model) {
        model.addAttribute("errorCode", "503");
        model.addAttribute("errorTitle", "Website đang bảo trì");
        model.addAttribute("errorMessage", "Chức năng này đang được bảo trì và nâng cấp.");
        model.addAttribute("suggestion", "Vui lòng quay lại sau để sử dụng chức năng này.");
        return "maintenance";
    }
}

