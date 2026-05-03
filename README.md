# RyRo - Fruit Ninja AI

RyRo là một trò chơi chém trái cây tương tác sử dụng trí tuệ nhân tạo để nhận diện hành động qua camera trước. Được thiết kế đặc biệt cho trẻ em, trò chơi mang lại trải nghiệm vận động vui vẻ và an toàn ngay trên thiết bị di động hoặc máy tính bảng.

## ✨ Tính năng nổi bật
- **AI Pose Detection:** Sử dụng Google ML Kit để nhận diện chuyển động của cổ tay trong thời gian thực.
- **Gameplay tương tác:** Người chơi dùng tay để chém trái cây bay trên màn hình mà không cần chạm vào thiết bị.
- **Hệ thống Bom (💣):** Tăng thử thách bằng cách tránh chém trúng bom để không bị trừ điểm.
- **Combo System:** Chém liên tiếp nhiều trái cây để nhận điểm thưởng cao và hiệu ứng rực rỡ.
- **Hiệu ứng Splatter:** Hiệu ứng bắn nước đầy màu sắc và vật lý trái cây bị chém đôi sinh động.
- **Tối ưu hóa máy tính bảng:** Hiển thị tốt nhất trên các dòng màn hình lớn như Samsung Galaxy Tab S9.

## 🛠 Công nghệ sử dụng
- **Ngôn ngữ:** Kotlin
- **UI Framework:** Jetpack Compose
- **Game Engine Logic:** Custom Logic với Coroutines
- **AI/ML:** Google ML Kit Pose Detection
- **Camera:** CameraX API

## 🚀 Hướng dẫn cài đặt
1. Clone project: `git clone https://github.com/[your-username]/ryro_chemtraicay.git`
2. Mở project bằng Android Studio (Koala trở lên).
3. Đảm bảo thiết bị chạy Android API 26 (Android 8.0) trở lên.
4. Build và Run ứng dụng trên thiết bị có camera trước.

## 📁 Cấu trúc thư mục chính
- `app/src/main/java/.../game/`: Chứa logic xử lý game và ViewModel.
- `app/src/main/java/.../services/`: Chứa dịch vụ nhận diện Pose Detection.
- `app/src/main/java/.../MainActivity.kt`: Giao diện chính và tích hợp CameraX.

## 📝 Lưu ý
Để có trải nghiệm tốt nhất, hãy đứng cách camera khoảng 1-2 mét và đảm bảo môi trường có đủ ánh sáng để AI nhận diện tốt nhất.

---
© 2024 RyRo Team
