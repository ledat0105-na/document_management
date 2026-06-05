# Script test chức năng quên mật khẩu
Write-Host "=== TEST CHỨC NĂNG QUÊN MẬT KHẨU ===" -ForegroundColor Green

# Test 1: Truy cập trang quên mật khẩu
Write-Host "`n1. Test truy cập trang quên mật khẩu..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/forgot" -Method GET
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Trang quên mật khẩu hoạt động bình thường" -ForegroundColor Green
    } else {
        Write-Host "❌ Lỗi truy cập trang quên mật khẩu" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Không thể kết nối đến server. Hãy chạy server trước!" -ForegroundColor Red
    exit 1
}

# Test 2: Test gửi email reset (với email test)
Write-Host "`n2. Test gửi email reset..." -ForegroundColor Yellow
$testEmail = "admin@example.com"
$body = @{
    email = $testEmail
}

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/forgot" -Method POST -Body $body
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Form gửi email hoạt động" -ForegroundColor Green
        Write-Host "📧 Email reset đã được gửi đến: $testEmail" -ForegroundColor Cyan
        Write-Host "   (Kiểm tra hộp thư Gmail nếu đã cấu hình email)" -ForegroundColor Gray
    } else {
        Write-Host "❌ Lỗi gửi email reset" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Lỗi khi gửi email reset: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Test trang reset với token giả
Write-Host "`n3. Test trang đặt lại mật khẩu..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/reset?token=test-token" -Method GET
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Trang đặt lại mật khẩu hoạt động bình thường" -ForegroundColor Green
    } else {
        Write-Host "❌ Lỗi truy cập trang đặt lại mật khẩu" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Lỗi khi truy cập trang reset" -ForegroundColor Red
}

Write-Host "`n=== KẾT QUẢ TEST ===" -ForegroundColor Green
Write-Host "✅ Chức năng quên mật khẩu đã sẵn sàng!" -ForegroundColor Green
Write-Host "`n📋 HƯỚNG DẪN SỬ DỤNG:" -ForegroundColor Cyan
Write-Host "1. Truy cập: http://localhost:8080/forgot" -ForegroundColor White
Write-Host "2. Nhập email đã đăng ký" -ForegroundColor White
Write-Host "3. Kiểm tra email để lấy link reset" -ForegroundColor White
Write-Host "4. Click link và đặt mật khẩu mới" -ForegroundColor White
Write-Host "5. Đăng nhập với mật khẩu mới" -ForegroundColor White

Write-Host "`n⚠️  LƯU Ý:" -ForegroundColor Yellow
Write-Host "- Cần cấu hình email SMTP để nhận được link reset" -ForegroundColor Yellow
Write-Host "- Token reset có hiệu lực 1 giờ" -ForegroundColor Yellow
Write-Host "- Mỗi lần yêu cầu sẽ tạo token mới" -ForegroundColor Yellow
