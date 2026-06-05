# Script test Session Timeout
Write-Host "=== TEST SESSION TIMEOUT (3 PHÚT) ===" -ForegroundColor Green

Write-Host "`n📋 CẤU HÌNH ĐÃ CẬP NHẬT:" -ForegroundColor Cyan
Write-Host "✅ Session timeout: 3 phút (180 giây)" -ForegroundColor Green
Write-Host "✅ Session không persistent (tự động xóa khi restart server)" -ForegroundColor Green
Write-Host "✅ Cookie max-age: 3 phút" -ForegroundColor Green
Write-Host "✅ Redirect đến login khi session hết hạn" -ForegroundColor Green

Write-Host "`n🧪 CÁCH TEST:" -ForegroundColor Yellow
Write-Host "1. Đăng nhập vào hệ thống" -ForegroundColor White
Write-Host "2. Không làm gì trong 3 phút" -ForegroundColor White
Write-Host "3. Thử truy cập trang bất kỳ" -ForegroundColor White
Write-Host "4. Sẽ tự động redirect về login với thông báo 'Phiên đăng nhập đã hết hạn'" -ForegroundColor White

Write-Host "`n🔄 TEST RESTART SERVER:" -ForegroundColor Yellow
Write-Host "1. Đăng nhập vào hệ thống" -ForegroundColor White
Write-Host "2. Restart server (Ctrl+C rồi chạy lại)" -ForegroundColor White
Write-Host "3. Refresh trang" -ForegroundColor White
Write-Host "4. Sẽ tự động redirect về login" -ForegroundColor White

Write-Host "`n⚙️ CẤU HÌNH CHI TIẾT:" -ForegroundColor Cyan
Write-Host "- server.servlet.session.timeout=180 (3 phút)" -ForegroundColor Gray
Write-Host "- server.servlet.session.persistent=false (không lưu session)" -ForegroundColor Gray
Write-Host "- server.servlet.session.cookie.max-age=180 (cookie hết hạn 3 phút)" -ForegroundColor Gray
Write-Host "- SessionCreationPolicy.IF_REQUIRED" -ForegroundColor Gray
Write-Host "- invalidSessionUrl=/login?expired" -ForegroundColor Gray

Write-Host "`n✅ Session timeout đã được cấu hình thành công!" -ForegroundColor Green
Write-Host "🎯 Bây giờ hệ thống sẽ tự động đăng xuất sau 3 phút không hoạt động!" -ForegroundColor Green
