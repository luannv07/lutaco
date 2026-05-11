# REPORT

## 1) Mục tiêu refactor
- Gom 2 enum gần như giống nhau là `FrequentType` và `Period` về một enum chung.
- Chuẩn hóa các helper xử lý ngày/giờ đang bị lặp lại ở nhiều service.
- Giữ tương thích dữ liệu cũ trong DB để không làm vỡ hệ thống hiện tại.

## 2) Những gì đã thay đổi

### 2.1. Gộp enum date/period về `Period`
- `Period` giờ là enum dùng chung cho:
  - Budget period
  - Recurring transaction frequency
- `Period` hỗ trợ alias cho dữ liệu cũ:
  - `DAY` ↔ `DAILY`
  - `WEEK` ↔ `WEEKLY`
  - `MONTH` ↔ `MONTHLY`
  - `YEAR` ↔ `YEARLY`
- `Period` có thêm helper:
  - `addTo(LocalDate)`
  - `calculateNextDate(LocalDate)` để tương thích logic cũ
  - `calculateEndDate(LocalDate)` cho budget

### 2.2. Dọn legacy enum `FrequentType`
- `FrequentType` đã được chuyển thành lớp legacy mỏng, chỉ còn vai trò tương thích tài liệu/code cũ.
- Code nghiệp vụ đã chuyển sang dùng `Period`.

### 2.3. Chuẩn hóa lưu/đọc DB cho recurring period
- Thêm `PeriodConverter` để map enum `Period` ↔ chuỗi DB.
- Nhờ converter + alias, dữ liệu cũ lưu theo `DAILY/WEEKLY/...` vẫn đọc được.
- Dữ liệu mới sẽ ghi theo chuẩn `DAY/WEEK/MONTH/YEAR`.

### 2.4. Utils hóa xử lý date/time lặp lại
- Bổ sung trong `TimeUtils`:
  - `today()`
  - `toUtcStartInstant(LocalDate)`
  - `toUtcExclusiveEndInstant(LocalDate)`
- Refactor các chỗ đang tự convert `LocalDate -> Instant` bằng tay.

### 2.5. Refactor các service/entity/response/repository
- `Budget`, `BudgetResponse`, `BudgetServiceImpl`, `BudgetRepository`
- `RecurringTransaction`, `RecurringTransactionResponse`, `RecurringTransactionServiceImpl`, `RecurringTransactionJob`
- Tất cả đã chuyển sang dùng enum chung `Period`.
- Filter parse cũng dùng `EnumUtils.tryFrom(...)` để sạch hơn.

### 2.6. Cải thiện `EnumUtils`
- `EnumUtils.from(...)` hỗ trợ alias theo interface `EnumAliasMatcher`.
- Thêm `EnumUtils.tryFrom(...)` để parse filter an toàn, tránh phải try/catch lặp lại.

## 3) Kết quả kiểm tra
- Maven build đã chạy thành công:
  - `./mvnw -q test -DskipTests`
- Kiểm tra lỗi IDE cho file recurring sau cùng cũng đã sạch.

## 4) Ghi chú tương thích
- Nếu frontend/API cũ vẫn gửi `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY` thì backend vẫn hiểu.
- Nếu DB cũ đang lưu recurring type theo dạng cũ, converter vẫn đọc được.
- `PeriodRange` của dashboard vẫn giữ riêng vì đây là nhóm range báo cáo khác với period/frequency của budget/recurring.

## 5) Ý tưởng xây dựng API cho Dashboard

### 5.1. Mục tiêu
Dashboard nên trả về dữ liệu nhanh, ít round-trip và dễ mở rộng theo từng widget.

### 5.2. Nhóm endpoint đề xuất
1. `GET /api/v1/dashboard/summary?period=LAST_1_MONTH`
   - Tổng thu/chi
   - Số giao dịch
   - Biến động so với kỳ trước

2. `GET /api/v1/dashboard/cashflow?period=LAST_1_MONTH`
   - Dòng tiền theo ngày/tuần/tháng
   - Dùng cho line chart

3. `GET /api/v1/dashboard/categories/top-expense?period=LAST_1_MONTH`
   - Top category chi tiêu
   - Tỷ trọng từng nhóm

4. `GET /api/v1/dashboard/budgets/status?period=LAST_1_MONTH`
   - Budget nào đang normal/warning/danger
   - % sử dụng, số tiền còn lại

5. `GET /api/v1/dashboard/recurring/health`
   - Số job recurring active/inactive
   - Job sắp đến hạn, job lỗi gần đây

6. `GET /api/v1/dashboard/export/basic`
7. `GET /api/v1/dashboard/export/advanced`
   - Xuất Excel/PDF theo từng level

### 5.3. Gợi ý thiết kế response
- Tách `summary`, `charts`, `tables`, `alerts` thành các block riêng.
- Dùng 1 `PeriodRange` chung cho dashboard để query thống nhất.
- Nếu dữ liệu lớn, nên cache summary theo user + period.

### 5.4. Gợi ý kỹ thuật
- Dùng projection/DTO thay vì trả entity.
- Tách service đọc dữ liệu dashboard riêng, tránh nhét logic vào controller.
- Nếu cần realtime thì có thể bổ sung endpoint refresh hoặc websocket sau.

---

Nếu cần, bước tiếp theo có thể là:
- chuẩn hóa luôn `DashboardServiceImpl` đang `UnsupportedOperationException`, hoặc
- viết tiếp các endpoint dashboard thật theo những ý tưởng ở trên.

