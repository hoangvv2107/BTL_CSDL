# Báo cáo kiểm thử chức năng

Ngày kiểm thử: 2026-11-18

## Môi trường và phạm vi

- Project Java Swing đã biên dịch bằng JDK 21 với `mysql-connector-j-9.5.0.jar`.
- MySQL tại `localhost:3306` kết nối được tới database `Trangtrai`.
- Đã kiểm tra bằng dữ liệu hiện có trong database. Smoke test CRUD dùng mã tạm
  `ZZ901` trong transaction và rollback, nên không làm thay đổi dữ liệu.
- Đây là kiểm thử mã nguồn, truy vấn và transaction; chưa có bộ kiểm thử GUI
  tự động để click từng nút và chụp kết quả trên màn hình.

## Kết quả theo yêu cầu trong ảnh

| Mã | Chức năng | Kết quả | Bằng chứng |
|---|---|---|---|
| F1 | Form quản lý lô nhập: thêm, sửa, xóa; các trường `MaLo`, loài, số lượng, tổng cân nặng, đơn giá, `MaPhieuNhap` | **Đạt qua mã nguồn và CRUD transaction** | `src/LoNhapPanel.java`: 10-89; có `INSERT`, `UPDATE`, `DELETE`, tải danh sách và chỉ nạp phiếu nhập loại `Con giống` |
| F2 | Form quản lý động vật: thêm, sửa, xóa; chọn chuồng, lô, phiếu xuất hoặc để trống | **Đạt qua mã nguồn và CRUD transaction** | `src/DongVatPanel.java`: 9-75; có các combo khóa ngoại, giá trị rỗng cho `MaPhieuXuat`, và truy vấn CRUD |
| F3 | Tìm kiếm theo mã động vật hoặc ghi chú | **Đạt** | `src/SearchPanel.java`: 36-49; truy vấn dùng `LIKE` trên `MaDongVat` và `GhiChu` |
| F4 | Lọc kết hợp theo chuồng, loài và trạng thái đang nuôi/đã bán | **Đạt qua truy vấn** | `src/SearchPanel.java`: 39-45; smoke test tìm kiếm theo mã trả về 8 dòng; trạng thái dựa trên `MaPhieuXuat IS NULL/IS NOT NULL` |
| F5 | Báo cáo thống kê theo chuồng: số con đang nuôi, tổng cân nặng, chỗ trống | **Đạt qua truy vấn** | `src/ReportChuongPanel.java`: 19-25; dùng `LEFT JOIN`, loại con đã bán và tính `SucChua - COUNT` |
| F6 | Báo cáo doanh thu theo khoảng ngày: phiếu xuất, khách hàng, số con, cân nặng, đơn giá, doanh thu | **Đạt qua truy vấn** | `src/RevenuePanel.java`: 23-32; dùng `BETWEEN`, `COUNT`, và `TongCanNangLucXuat * DonGiaBan` |
| F7 | Xuất báo cáo thống kê theo chuồng ra Excel | **Đã triển khai, biên dịch PASS** | `src/ReportChuongPanel.java`: 12-20 gọi `Ui.exportExcel(...)`; `src/Ui.java`: 40-70 tạo file `.xlsx` bằng Apache POI |
| F8 | Xuất báo cáo doanh thu ra Excel | **Đã triển khai, biên dịch PASS** | `src/RevenuePanel.java`: 16-21 gọi `Ui.exportExcel(...)`; `src/Ui.java`: 40-70 tạo file `.xlsx` bằng Apache POI |

## Kết quả smoke test CSDL

- Biên dịch: **PASS**, sinh đủ các class trong thư mục `out`.
- Kết nối MySQL: **PASS**.
- Dữ liệu kiểm tra: 3 lô nhập, 12 động vật, 5 chuồng, 2 phiếu xuất.
- Trạng thái động vật: 9 đang nuôi, 3 đã bán.
- Báo cáo chuồng trả đủ 5 chuồng, gồm cả chuồng chưa có vật nuôi.
- Báo cáo doanh thu từ `2026-10-01` đến `2026-11-30` trả 2 phiếu:
  - `PX001`: 1 con, doanh thu `13,747,500`.
  - `PX002`: 2 con, doanh thu `552,000`.
- Transaction CRUD lô nhập và động vật: **PASS**, sau đó rollback thành công.
- Chức năng xuất Excel: **PASS ở mức mã nguồn và dependency**; đã xác nhận có `JFileChooser`, tự thêm đuôi `.xlsx`, ghi tiêu đề/các dòng dữ liệu, tự căn độ rộng cột và hiển thị thông báo thành công.
- Khi xuất Excel đã gặp `NoClassDefFoundError: org/apache/logging/log4j/Logger`.
- Nguyên nhân: Apache POI thiếu dependency Log4j trong classpath.
- Đã bổ sung `log4j-api-2.24.3.jar`, `log4j-core-2.24.3.jar` và cập nhật `BTL_CSDL.iml`.

## Chức năng/chất lượng còn chưa hoàn tất hoặc chưa được xác nhận

1. **Chưa có kiểm thử GUI tự động hoặc kịch bản regression**: chưa xác nhận bằng thao tác trực tiếp việc chọn đường dẫn, ghi file, mở lại file `.xlsx`, ghi đè file cũ và thông báo lỗi khi không có quyền ghi.
2. **Chưa có form quản lý phiếu xuất bán và khách hàng trong project**: `MaPhieuXuat` của động vật chỉ chọn từ dữ liệu đã tồn tại trong database. Nếu yêu cầu đầy đủ CRUD cho phiếu xuất/khách hàng, chức năng này hiện chưa được triển khai.
3. **Chưa có kiểm thử tự động cho dữ liệu biên**: ngày sai định dạng, ngày bắt đầu lớn hơn ngày kết thúc, số âm, mã trùng, khóa ngoại không tồn tại và xóa bản ghi đang được tham chiếu mới chỉ phụ thuộc vào validation của CSDL/hiển thị lỗi.
4. **Cần xác nhận lại dữ liệu mẫu**: comment SQL ghi 13 động vật nhưng database đang kiểm thử chỉ có 12. Đây là lệch dữ liệu mẫu, không phải lỗi truy vấn giao diện.

## Kết luận

Các nhóm chức năng trong ảnh và chức năng xuất Excel cho hai báo cáo đã có phần triển khai tương ứng. Xuất file đã được kiểm tra ở mức mã nguồn/dependency và biên dịch; cần kiểm thử thủ công trên GUI để xác nhận trọn vẹn thao tác chọn nơi lưu và mở file kết quả.
