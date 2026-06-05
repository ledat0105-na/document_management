# Hướng dẫn test chức năng Quên mật khẩu

## ✅ Chức năng đã có sẵn:

### 1. **Trang quên mật khẩu**: `http://localhost:8080/forgot`
- Form nhập email
- Gửi link reset qua email

### 2. **Trang đặt lại mật khẩu**: `http://localhost:8080/reset?token=xxx`
- Form nhập mật khẩu mới
- Xác nhận mật khẩu
- Có nút hiện/ẩn mật khẩu

### 3. **Logic xử lý**:
- Tạo token ngẫu nhiên
- Token hết hạn sau 1 giờ
- Gửi email HTML đẹp
- Validate mật khẩu (tối thiểu 6 ký tự)

## 🔧 Cấu hình Email (cần thiết):

### Cách 1: Sử dụng Gmail
1. Tạo App Password cho Gmail
2. Cập nhật `application.properties`:
```properties
spring.mail.username=yourgmail@gmail.com
spring.mail.password=your-app-password
```

### Cách 2: Sử dụng biến môi trường
```bash
set MAIL_USERNAME=yourgmail@gmail.com
set MAIL_PASSWORD=your-app-password
```

## 🧪 Test chức năng:

### Bước 1: Truy cập trang quên mật khẩu
- URL: `http://localhost:8080/forgot`
- Nhập email đã đăng ký

### Bước 2: Kiểm tra email
- Mở hộp thư Gmail
- Tìm email "Đặt lại mật khẩu DocManager"
- Click link trong email

### Bước 3: Đặt mật khẩu mới
- Nhập mật khẩu mới (tối thiểu 6 ký tự)
- Xác nhận mật khẩu
- Click "Đặt lại mật khẩu"

### Bước 4: Đăng nhập với mật khẩu mới
- URL: `http://localhost:8080/login`
- Sử dụng email và mật khẩu mới

## ⚠️ Lưu ý:
- Token chỉ có hiệu lực 1 giờ
- Mỗi lần yêu cầu reset sẽ tạo token mới
- Token cũ sẽ bị vô hiệu hóa
- Email phải được cấu hình đúng để nhận được link reset

## 🐛 Troubleshooting:
- **Không nhận được email**: Kiểm tra cấu hình SMTP
- **Token không hợp lệ**: Token đã hết hạn hoặc đã sử dụng
- **Email không tồn tại**: Sử dụng email đã đăng ký trong hệ thống
