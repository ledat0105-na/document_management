========================================
    HƯỚNG DẪN NHANH CHO KHÁCH HÀNG
    DOCUMENT MANAGEMENT SYSTEM
========================================

🎯 CHẠY ỨNG DỤNG VỚI MYSQL - CHỈ 3 BƯỚC:

BƯỚC 1: Chuẩn bị MySQL
- Đảm bảo MySQL Server đang chạy
- Tạo database: CREATE DATABASE document_management;

BƯỚC 2: Chạy ứng dụng
- Double-click file: RUN_WITH_MYSQL.bat
- Hoặc chạy: start_app_simple.bat (sau khi chạy switch_to_mysql.bat)

BƯỚC 3: Truy cập
- Mở trình duyệt: http://localhost:8080
- Đăng nhập: admin@example.com / admin123

========================================
    THÔNG TIN QUAN TRỌNG
========================================

🔧 CẤU HÌNH MYSQL:
- Username: root
- Password: lambieng
- Database: document_management
- Host: localhost:3306

⚠️ NẾU PASSWORD MYSQL KHÁC:
- Sửa file application.properties
- Thay "lambieng" bằng password MySQL của bạn

========================================
    FILE QUAN TRỌNG
========================================

📁 CÁC FILE CẦN THIẾT:
- RUN_WITH_MYSQL.bat        : Chạy với MySQL (KHUYẾN NGHỊ)
- start_app_simple.bat      : Chạy ứng dụng
- switch_to_mysql.bat       : Chuyển sang MySQL
- application.properties    : Cấu hình database
- document_management-*.jar : File ứng dụng

========================================
    XỬ LÝ LỖI
========================================

❌ LỖI "Java chưa được cài đặt":
✅ Tải Java 17 từ: https://adoptium.net/

❌ LỖI "Cannot connect to MySQL":
✅ Kiểm tra MySQL Server đang chạy

❌ LỖI "Port 8080 đã được sử dụng":
✅ Đóng ứng dụng khác đang dùng port này

❌ LỖI "Database does not exist":
✅ Tạo database: CREATE DATABASE document_management;

========================================
    LIÊN HỆ HỖ TRỢ
========================================

Nếu gặp vấn đề, liên hệ nhà phát triển với:
- Thông báo lỗi cụ thể
- Phiên bản MySQL
- Phiên bản Java

========================================
    CHÚC BẠN THÀNH CÔNG!
========================================
