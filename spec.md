# ARCHITECTURE & UI REVAMP SPEC (KOTLIN NATIVE)

## 1. Source Code Consistency
- **Pattern:** MVVM (Model-View-ViewModel).
- **Navigation:** Sử dụng Jetpack Compose Navigation với hiệu ứng chuyển cảnh 'Slide and Fade'.
- **Language:** 100% Tiếng Việt (Localization via strings.xml).

## 2. UI Standards (Khan Academy Inspired)
- **Backgrounds:** Sử dụng Gradient mềm hoặc Soft Patterns (Mây/Sao).
- **Interactive:** Mọi vật thể chạm được phải có hiệu ứng 'Spring Animation'.
- **Layout:** Z-Index rõ ràng: Camera (0) -> Game/Content (1) -> UI Overlay (2).

## 3. High-Quality Infographics (Science Module)
- **Assets:** Tuyệt đối không vẽ thủ công. Sử dụng Lottie JSON cho các hành tinh và hiệu ứng vũ trụ.
- **Micro-interactions:** Bé chạm vào đâu, chỗ đó phải phản hồi (phóng to, phát tiếng Việt).