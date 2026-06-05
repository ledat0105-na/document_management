========================================
    DocManager - Document Management
========================================

YÊU CẦU HỆ THỐNG:
- JDK 17+ (Java Development Kit)
- MySQL Server đang chạy
- Port 8080 không bị sử dụng

CÁCH CHẠY:
1. Đảm bảo MySQL đang chạy với thông tin:
   - Host: localhost
   - Port: 3306
   - Username: root
   - Password: lambieng

2. Double-click vào file "start.exe" hoặc "start.bat"

3. Đợi ứng dụng khởi động (khoảng 10-30 giây)

4. Trình duyệt sẽ tự động mở http://localhost:8080

5. Nếu không tự mở, hãy mở trình duyệt và vào: http://localhost:8080

CÁCH DỪNG:
- Đóng cửa sổ Command Prompt
- Hoặc nhấn Ctrl+C trong cửa sổ đang chạy

CẤU HÌNH:
- File application.properties: cấu hình database, email, port
- Thư mục storage: lưu trữ file tài liệu

HỖ TRỢ:
- Nếu gặp lỗi, kiểm tra:
  + MySQL có đang chạy không
  + Port 8080 có bị chiếm không
  + JDK 17+ đã cài chưa

========================================
