# Android Studio Agent Specification: Fruit Ninja AI

## 1. Project Context
- **Project Type:** Flutter Mobile App.
- **Target Device:** Samsung Galaxy Tab S9 (High-end Tablet).
- **IDE:** Android Studio (Cần tối ưu Gradle và Android Manifest).
- **Primary Goal:** Trò chơi chém trái cây bằng camera trước cho trẻ em.

## 2. Technical Stack & Dependencies
Agent hãy tự động cập nhật `pubspec.yaml` với các phiên bản tương thích:
- `flame`: Game engine để xử lý render 60-120 FPS.
- `google_mlkit_pose_detection`: Nhận diện khung xương (Pose).
- `camera`: Quản lý luồng stream video từ camera trước.
- `google_fonts`: Sử dụng font chữ vui tươi cho trẻ em.

## 3. Native Android Configuration (CRITICAL)
Agent cần thực hiện các thay đổi sau trong thư mục `/android`:
- **build.gradle (app):**
    - `minSdkVersion`: 21 (Yêu cầu tối thiểu của ML Kit).
    - `targetSdkVersion`: 34 (Android 14).
- **AndroidManifest.xml:**
    - Thêm quyền: `<uses-permission android:name="android.permission.CAMERA" />`.
    - Force Orientation: `<activity ... android:screenOrientation="landscape">`.

## 4. Feature Implementation Logic
### 4.1. Pose Detection Service
- Sử dụng `PoseDetectorOptions` với chế độ `InputImage.fromBytes` để xử lý frame từ camera.
- Chỉ lấy tọa độ của `PoseLandmarkType.leftWrist` và `PoseLandmarkType.rightWrist`.
- **Lưu ý:** Cần đảo ngược tọa độ (Mirroring) vì sử dụng camera trước.

### 4.2. Flame Game Component
- **BladeComponent:** Tạo một "lưỡi kiếm" ảo đi theo tọa độ cổ tay. Sử dụng `PositionComponent`.
- **FruitComponent:** Trái cây bay theo quỹ đạo parabol (Projective Motion).
- **Collision Detection:** Sử dụng `HasCollisionDetection` mixin của Flame để check va chạm giữa Blade và Fruit.

## 5. Agent Instructions for Android Studio
1. **Khởi tạo:** Kiểm tra cấu hình `sdk.dir` và `flutter.sdk` để đảm bảo môi trường build không lỗi.
2. **Viết Code:** Chia nhỏ các class vào đúng thư mục `/lib/services`, `/lib/game`, `/lib/widgets`.
3. **Comment:** Giải thích các hàm xử lý tọa độ bằng tiếng Việt.
4. **Tối ưu:** Đảm bảo giải phóng bộ nhớ (dispose camera và pose detector) khi không sử dụng để tránh lag máy.
