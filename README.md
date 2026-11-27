# Doom Weather (`vn.linh.doomweather`)

Ứng dụng thời tiết Android tối ưu cho **Samsung Galaxy S26 (One UI / Android 17)** với widget 4×2 phong cách iOS + chủ đề MCU & Doomsday.

## Phiên bản hiện tại: `v1.2.7` (`versionCode = 10`)

### Các giao diện hỗ trợ (9 theme)
`doomsday` (Doomsday 2 Teams), `doom` (Doctor Doom), `mcu`, `mcu_ironman`, `mcu_spiderman`, `mcu_thor`, `mcu_strange`, `ios`, `wallpaper`.

### Quy ước thiết kế Widget (QUAN TRỌNG)
- **Sạch 100%, KHÔNG thêm bất kỳ nút hay icon thừa nào bên ngoài widget.**
- **3 vùng chạm ẩn (trong suốt hoàn toàn)** trong `res/layout/weather_widget.xml`:
  1. **Góc trên-trái (`36%` chiều cao, `58%` chiều rộng — phủ tên vị trí):** mở `LocationRefreshActivity` (Activity trong suốt, chạy foreground ~1 giây, dò song song `fused` / `gps` / `network`) để cập nhật GPS + thời tiết ngay lập tức mà không bị Android 14–17 bóp quyền nền.
  2. **Dưới-trái (`64%` chiều cao, `58%` chiều rộng — nhiệt độ & dự báo 6h):** mở `MainActivity`.
  3. **Nửa phải (`42%` chiều rộng — nhân vật):** xoay vòng đổi giao diện riêng cho widget đó (`Store.cycleWidgetTheme`).
- **Hỗ trợ Samsung Widget Stack (Ngăn xếp tiện ích):** mỗi widget lưu theme độc lập theo `appWidgetId` trong `Store.java` để xếp chồng và vuốt trái/phải mượt ngay trên màn hình chính S26.
- **Typography (`WidgetPainter.java`):** dùng `sans-serif-medium` cho tên khu vực / dự báo, `sans-serif-light` nén `-0.05em` cho nhiệt độ lớn, căn giữa quang học tên khu vực ngắn (VD: `南砂`) trên phần chữ số nhiệt độ (bỏ qua dấu `°`), scale đồng tỉ lệ không méo chữ, và tự vẽ lại ngay khi cập nhật APK (`MY_PACKAGE_REPLACED`).

### Build APK nhanh (không cần tải dependency ngoài)
`WeatherClient.java` dùng sẵn `HttpURLConnection` của Android SDK nên có thể đóng gói & ký APK trực tiếp bằng Android SDK Build Tools (`aapt2`, `javac`, `d8`, `zipalign`, `apksigner`) rồi tải lên Google Drive (`gdrive mutate upload`) để cài trực tiếp lên S26.
