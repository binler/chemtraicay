# Kotlin Specification: Fruit Ninja AI & Learning Ecosystem

## 1. Technical Stack
- **Language:** Kotlin (100%).
- **UI Framework:** Jetpack Compose (Modern UI).
- **AI/ML:** Google ML Kit Pose Detection (Client-side).
- **Camera:** CameraX (Analysis mode for ML Kit).
- **Animation:** Compose Animation (Lottie for complex animations).

## 2. UI/UX Guidelines (Focus: 2-5 Years Old)
- **Visuals:** Bright Pastel Palette, Rounded Corners (CornerRadius 24dp+).
- **Language:** 100% Tiếng Việt (Localization: strings.xml).
- **Interaction:** Haptic feedback (Rung nhẹ) khi bé làm đúng bài tập.
- **Orientation:** Lock Landscape (android:screenOrientation="landscape").

## 3. Architecture
- **Pattern:** MVVM (Model-View-ViewModel).
- **State:** Use `StateFlow` to update wrist coordinates from CameraX to UI.
- **Component:**
  - `/ui/theme/`: Định nghĩa hệ màu sắc và Typography tiếng Việt.
  - `/ui/components/`: Các Widget học tập (Thẻ bài, Nút bấm lớn).
  - `/ml/`: PoseDetector analyzer.

## 4. Animation Goals
- Tạo hiệu ứng 'chuyển cảnh mềm' (Smooth transitions) giữa các phân khu: Trò chơi, Học tập và Khoa học.
- Sử dụng Lottie để hiển thị các nhân vật hoạt hình động hướng dẫn bé bằng tiếng Việt.