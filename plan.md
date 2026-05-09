Dưới đây là tư vấn chi tiết về thiết kế và phân bổ nội dung bằng tiếng Việt:

---

## 1. Bảng màu & Phong cách hình ảnh (Visual Style)

Trẻ em ở độ tuổi này phản ứng rất mạnh với màu sắc. Bạn nên yêu cầu Agent hoặc bộ phận thiết kế áp dụng các quy tắc sau:

* **Bảng màu:** Sử dụng các màu sắc tươi sáng, bão hòa cao (Vibrant Colors).
* **Màu vàng/Cam:** Khơi gợi sự sáng tạo và năng lượng.
* **Màu xanh lá:** Tạo cảm giác an toàn, phù hợp cho mục "Nghiên cứu khoa học".
* **Màu xanh dương:** Giúp tập trung, phù hợp cho mục "Học tập".


* **Phong cách hình ảnh:** Sử dụng **Vector Art** với các đường nét bo tròn (Soft corners). Tuyệt đối tránh các góc nhọn để tạo cảm giác thân thiện. Các icon nên to, rõ ràng vì trẻ 2 tuổi chưa có sự khéo léo cao ở đầu ngón tay.

---

## 2. Thiết kế mục "Học tập" & "Nghiên cứu khoa học"

### **A. Phân chia theo độ tuổi (Age-Based Content)**

| Hạng mục | Bé 2 tuổi (Khám phá giác quan) | Bé 5 tuổi (Tiền tiểu học) |
| --- | --- | --- |
| **Học tập** | **Nhận diện:** Màu sắc cơ bản, hình khối tròn/vuông, tiếng kêu của con vật. (Dùng tiếng Việt hoàn toàn). | **Tư duy:** Ghép chữ cái thành từ đơn, cộng trừ trong phạm vi 10, học từ vựng tiếng Anh (chủ đề hoa quả, gia đình). |
| **Khoa học** | **Quan sát:** Thời tiết (nắng/mưa/tuyết), các bộ phận trên cơ thể (mắt, mũi, miệng). | **Giải thích:** Vòng đời của cây (hạt -> cây), hệ mặt trời, tại sao lại có mưa. |

### **B. Infographic tương tác cho Khoa học**

Thay vì Infographic tĩnh, hãy yêu cầu Agent build theo dạng **"Khám phá từng lớp"**:

* *Ví dụ:* Một bức tranh về Trái Đất. Khi bé chạm vào lớp mây, mây sẽ biến mất để hiện ra đại dương. Khi chạm vào cá cá sẽ bơi. Toàn bộ chú thích hiển thị bằng font chữ tiếng Việt to, không chân (Sans-serif) để bé dễ nhìn.

---

## 3. Cấu trúc menu "Mở rộng" (Scalable Navigation)

Để ứng dụng chuyên nghiệp và dễ mở rộng, hãy thiết kế menu theo dạng **"Hành tinh tri thức"**:

* **Hành tinh Game:** Chứa 6 trò chơi hiện tại (Màu sắc chủ đạo: Đỏ/Cam).
* **Hành tinh Lớp học:** Phép tính, chữ cái (Màu chủ đạo: Xanh dương).
* **Hành tinh Phòng thí nghiệm:** Các Infographic khoa học (Màu chủ đạo: Xanh lá).

---

## 4. Những lưu ý đặc biệt về Ngôn ngữ & UX

* **Tiếng Việt 100%:** Ngoại trừ các thẻ học tiếng Anh, toàn bộ âm thanh chỉ dẫn (Voice-over) và văn bản phải là tiếng Việt chuẩn.
* *Mẹo:* Bạn có thể sử dụng các API Text-to-Speech (TTS) của Google để tạo giọng đọc tự động nếu không muốn tự ghi âm.


* **Phản hồi âm thanh (Audio Feedback):**
* Bé làm đúng: Tiếng "Ting ting" hoặc vỗ tay vui vẻ.
* Bé làm chưa đúng: Tiếng "Ồ ô" nhẹ nhàng, tránh dùng âm thanh gắt gây sợ hãi.


* **Tối ưu cho Tab S9:** Tận dụng diện tích màn hình lớn để đặt các nút bấm xa nhau, tránh việc bé 2 tuổi chạm nhầm khi đang vận động.

---

## 5. Mẫu Spec cập nhật cho Agent (Focus on UI/UX)

Bạn hãy dán đoạn này vào file `spec.md` để Agent trong Android Studio hiểu yêu cầu mới của bạn:

```markdown
## 9. UI/UX & Localization Requirements
- **Language:** Toàn bộ String trong ứng dụng (Text, Tooltips, Buttons) phải sử dụng tiếng Việt.
- **English Learning:** Chỉ sử dụng tiếng Anh trong các thẻ bài (Flashcards) mục "Học tập".
- **Visuals:** 
    - Sử dụng assets hình ảnh dạng 2D Flat Design, màu sắc bão hòa cao.
    - Icons cho bé 2 tuổi phải lớn tối thiểu 80x80 pixels.
- **Audio:** Tích hợp audio feedback bằng tiếng Việt (ví dụ: "Giỏi quá!", "Thử lại nào con").
- **Navigation:** Thiết kế hệ thống Menu có khả năng mở rộng để thêm nhiều hành tinh (Module) mới trong tương lai mà không cần thay đổi Layout chính.

```