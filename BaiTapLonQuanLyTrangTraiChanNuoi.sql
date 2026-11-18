DROP DATABASE IF EXISTS Trangtrai;
CREATE DATABASE IF NOT EXISTS Trangtrai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE Trangtrai;

-- PHẦN 1: TẠO CẤU TRÚC 14 BẢNG

-- 1. Bảng Nhân viên (Thực thể cha tổng quát)
CREATE TABLE NhanVien (
    MaNV CHAR(5) PRIMARY KEY,
    HoVaTen VARCHAR(100) NOT NULL,
    Sdt VARCHAR(15) NOT NULL,
    DiaChi VARCHAR(255),
    TrangThai VARCHAR(30) NOT NULL DEFAULT 'Đang làm việc' 
        CHECK (TrangThai IN ('Đang làm việc', 'Đã nghỉ việc'))
);

-- 2. Bảng Nhà cung cấp
CREATE TABLE NhaCungCap (
    MaNCC CHAR(5) PRIMARY KEY,
    TenNCC VARCHAR(150) NOT NULL,
    DiaChi VARCHAR(255),
    Sdt VARCHAR(15) NOT NULL,
    LoaiNCC VARCHAR(50) NOT NULL CHECK (LoaiNCC IN ('Con giống', 'Thức ăn'))
);

-- 3. Bảng Khách hàng
CREATE TABLE KhachHang (
    MaKH CHAR(5) PRIMARY KEY,
    TenKH VARCHAR(100) NOT NULL,
    Sdt VARCHAR(15) NOT NULL,
    DiaChi VARCHAR(255)
);

-- 4. Bảng Thức ăn
CREATE TABLE ThucAn (
    MaThucAn CHAR(5) PRIMARY KEY,
    TenThucAn VARCHAR(100) NOT NULL,
    DonViTinh VARCHAR(20) NOT NULL
);

-- 5. Bảng Chuồng
CREATE TABLE Chuong (
    MaChuong CHAR(5) PRIMARY KEY,
    TenChuong VARCHAR(100) NOT NULL,
    SucChua INT NOT NULL CHECK (SucChua > 0),
    ViTri VARCHAR(100)
);

-- 6. Bảng Nhân viên Thú y (Kế thừa NhanVien)
CREATE TABLE NhanVienThuY (
    MaNV CHAR(5) PRIMARY KEY,
    ChuyenMon VARCHAR(100),
    ChungChiHanhNghe VARCHAR(100),
    FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV) 
        ON UPDATE CASCADE 
        ON DELETE CASCADE
);

-- 7. Bảng Nhân viên Chăm sóc (Kế thừa NhanVien)
CREATE TABLE NhanVienChamSoc (
    MaNV CHAR(5) PRIMARY KEY,
    SoNamKinhNghiem INT NOT NULL DEFAULT 0 CHECK (SoNamKinhNghiem >= 0),
    ViTriPhuTrach VARCHAR(100),
    FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV) 
        ON UPDATE CASCADE 
        ON DELETE CASCADE
);

-- 8. Bảng Phiếu nhập (Nhận khóa ngoại MaNCC)
CREATE TABLE PhieuNhap (
    MaPhieuNhap CHAR(5) PRIMARY KEY,
    NgayNhap DATE NOT NULL,
    LoaiPhieuNhap VARCHAR(50) NOT NULL CHECK (LoaiPhieuNhap IN ('Con giống', 'Thức ăn')),
    MaNCC CHAR(5) NOT NULL,
    FOREIGN KEY (MaNCC) REFERENCES NhaCungCap(MaNCC) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT
);

-- 9. Bảng Phiếu xuất bán (Nhận khóa ngoại MaKH)
CREATE TABLE PhieuXuatBan (
    MaPhieuXuat CHAR(5) PRIMARY KEY,
    NgayXuat DATE NOT NULL,
    TongCanNangLucXuat DECIMAL(10, 2) NOT NULL CHECK (TongCanNangLucXuat > 0),
    DonGiaBan DECIMAL(15, 2) NOT NULL CHECK (DonGiaBan >= 0),
    MaKH CHAR(5) NOT NULL,
    FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT
);

-- 10. Bảng Lô nhập (Nhập động vật theo lô từ PhieuNhap)
CREATE TABLE LoNhap (
    MaLo CHAR(5) PRIMARY KEY,
    LoaiDongVat VARCHAR(50) NOT NULL,
    SoLuongNhap INT NOT NULL CHECK (SoLuongNhap > 0),
    TongCanNangLucNhap DECIMAL(10, 2) NOT NULL CHECK (TongCanNangLucNhap > 0),
    DonGiaNhap DECIMAL(15, 2) NOT NULL CHECK (DonGiaNhap >= 0),
    MaPhieuNhap CHAR(5) NOT NULL,
    FOREIGN KEY (MaPhieuNhap) REFERENCES PhieuNhap(MaPhieuNhap) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT
);

-- 11. Bảng Động vật (Nhận khóa ngoại từ Chuong, LoNhap, PhieuXuatBan)
CREATE TABLE DongVat (
    MaDongVat CHAR(5) PRIMARY KEY,
    GioiTinh VARCHAR(10) CHECK (GioiTinh IN ('Đực', 'Cái', 'Trống', 'Mái')),
    CanNangHienTai DECIMAL(10, 2) NOT NULL CHECK (CanNangHienTai > 0),
    TinhTrangSucKhoe VARCHAR(100),
    GhiChu TEXT,
    MaChuong CHAR(5) NOT NULL,
    MaLo CHAR(5) NOT NULL,
    MaPhieuXuat CHAR(5) DEFAULT NULL,
    FOREIGN KEY (MaChuong) REFERENCES Chuong(MaChuong) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    FOREIGN KEY (MaLo) REFERENCES LoNhap(MaLo) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    FOREIGN KEY (MaPhieuXuat) REFERENCES PhieuXuatBan(MaPhieuXuat) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT
);

-- 12. Bảng Hồ sơ bệnh án (Thực thể yếu theo DongVat, có bác sĩ thú y khám)
CREATE TABLE HoSoBenhAn (
    MaDongVat CHAR(5) NOT NULL,
    LanKham INT NOT NULL CHECK (LanKham > 0),
    NgayKham DATE NOT NULL,
    ChanDoan TEXT NOT NULL,
    DonThuoc TEXT NULL,
    MaNV CHAR(5) NOT NULL,
    PRIMARY KEY (MaDongVat, LanKham),
    FOREIGN KEY (MaDongVat) REFERENCES DongVat(MaDongVat) 
        ON UPDATE CASCADE 
        ON DELETE CASCADE,
    FOREIGN KEY (MaNV) REFERENCES NhanVienThuY(MaNV) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT
);

-- 13. Bảng Chăm sóc (Quan hệ N-N giữa NhanVienChamSoc và DongVat)
CREATE TABLE ChamSoc (
    MaNV CHAR(5) NOT NULL,
    MaDongVat CHAR(5) NOT NULL,
    PRIMARY KEY (MaNV, MaDongVat),
    FOREIGN KEY (MaNV) REFERENCES NhanVienChamSoc(MaNV) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    FOREIGN KEY (MaDongVat) REFERENCES DongVat(MaDongVat) 
        ON UPDATE CASCADE 
        ON DELETE CASCADE
);

-- 14. Bảng Gồm (Chi tiết nhập thức ăn - Quan hệ N-N giữa PhieuNhap và ThucAn)
CREATE TABLE Gom (
    MaPhieuNhap CHAR(5) NOT NULL,
    MaThucAn CHAR(5) NOT NULL,
    SoLuongNhap DECIMAL(10, 2) NOT NULL CHECK (SoLuongNhap > 0),
    DonGiaNhap DECIMAL(15, 2) NOT NULL CHECK (DonGiaNhap >= 0),
    PRIMARY KEY (MaPhieuNhap, MaThucAn),
    FOREIGN KEY (MaPhieuNhap) REFERENCES PhieuNhap(MaPhieuNhap) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    FOREIGN KEY (MaThucAn) REFERENCES ThucAn(MaThucAn) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT
);

-- PHẦN 2: THÊM DỮ LIỆU MẪU

-- 1. NHÂN VIÊN (5 người: 2 Thú y, 3 Chăm sóc)
INSERT INTO NhanVien (MaNV, HoVaTen, Sdt, DiaChi, TrangThai) VALUES
('NV001', 'Nguyễn Văn Hùng', '0912345678', 'Hà Đông, Hà Nội', 'Đang làm việc'),
('NV002', 'Trần Thị Mai Lan', '0987654321', 'Thanh Xuân, Hà Nội', 'Đang làm việc'),
('NV003', 'Lê Minh Tuấn', '0934567890', 'Chương Mỹ, Hà Nội', 'Đang làm việc'),
('NV004', 'Phạm Thị Thu Hương', '0945678901', 'Hoài Đức, Hà Nội', 'Đang làm việc'),
('NV005', 'Hoàng Văn Nam', '0967890123', 'Quốc Oai, Hà Nội', 'Đã nghỉ việc');

-- 2. NHÀ CUNG CẤP (Con giống & Thức ăn)
INSERT INTO NhaCungCap (MaNCC, TenNCC, DiaChi, Sdt, LoaiNCC) VALUES
('NCC01', 'Công ty CP Chăn nuôi C.P. Việt Nam', 'KCN Biên Hòa 2, Đồng Nai', '02513836251', 'Con giống'),
('NCC02', 'Trung tâm Nghiên cứu Gia cầm Thụy Phương', 'Bắc Từ Liêm, Hà Nội', '02438389773', 'Con giống'),
('NCC03', 'Công ty TNHH Cargill Việt Nam', 'Văn Lâm, Hưng Yên', '02213980456', 'Thức ăn');

-- 3. KHÁCH HÀNG
INSERT INTO KhachHang (MaKH, TenKH, Sdt, DiaChi) VALUES
('KH001', 'Cơ sở giết mổ tập trung Minh Hiếu', '0904112233', 'Thanh Oai, Hà Nội'),
('KH002', 'Chuỗi thực phẩm sạch Bác Tôm', '0915223344', 'Đống Đa, Hà Nội'),
('KH003', 'Nhà hàng đặc sản Gà Đồi Ba Vì', '0988334455', 'Ba Vì, Hà Nội');

-- 4. THỨC ĂN CHĂN NUÔI
INSERT INTO ThucAn (MaThucAn, TenThucAn, DonViTinh) VALUES
('TA001', 'Cám hỗn hợp Bio-zeem cho lợn thịt', 'Bao 25kg'),
('TA002', 'Cám đậm đặc vỗ béo bò thịt', 'Bao 25kg'),
('TA003', 'Cám viên tổng hợp cho gà thả vườn', 'Bao 25kg'),
('TA004', 'Ngô hạt nghiền phối trộn', 'Kg');

-- 5. CHUỒNG NUÔI (Đặt mã theo từng khu CHA, CHB, CHC, CHD)
INSERT INTO Chuong (MaChuong, TenChuong, SucChua, ViTri) VALUES
('CHA01', 'Chuồng Lợn thịt A1', 20, 'Khu A - Dãy 1'),
('CHA02', 'Chuồng Lợn thịt A2', 20, 'Khu A - Dãy 2'),
('CHB01', 'Chuồng Bò vỗ béo B1', 12, 'Khu B - Phía Đông'),
('CHC01', 'Chuồng Gà thả vườn C1', 60, 'Khu C - Phía Nam'),
('CHD01', 'Chuồng Cách ly & Điều trị D1', 10, 'Khu D - Phía Tây');

-- 6 & 7. PHÂN LOẠI NHÂN VIÊN (KẾ THỪA ISA)
INSERT INTO NhanVienThuY (MaNV, ChuyenMon, ChungChiHanhNghe) VALUES
('NV001', 'Bệnh học gia súc (Lợn, Bò)', 'CCHN-TY-2021-0142'),
('NV002', 'Bệnh học gia cầm & Tiêm phòng', 'CCHN-TY-2022-0589');

INSERT INTO NhanVienChamSoc (MaNV, SoNamKinhNghiem, ViTriPhuTrach) VALUES
('NV003', 5, 'Phụ trách Khu A (Chuồng Lợn)'),
('NV004', 3, 'Phụ trách Khu B & Khu C (Bò, Gà)'),
('NV005', 2, 'Phụ trách vệ sinh chuồng trại');

-- 8. PHIẾU NHẬP (2 phiếu nhập giống, 2 phiếu nhập thức ăn)
INSERT INTO PhieuNhap (MaPhieuNhap, NgayNhap, LoaiPhieuNhap, MaNCC) VALUES
('PN001', '2026-07-02', 'Con giống', 'NCC01'),
('PN002', '2026-08-10', 'Con giống', 'NCC02'),
('PN003', '2026-09-05', 'Thức ăn', 'NCC03'),
('PN004', '2026-10-12', 'Thức ăn', 'NCC03');

-- 9. PHIẾU XUẤT BÁN (2 đợt xuất chuồng)
INSERT INTO PhieuXuatBan (MaPhieuXuat, NgayXuat, TongCanNangLucXuat, DonGiaBan, MaKH) VALUES
('PX001', '2026-10-22', 211.50, 65000, 'KH001'),
('PX002', '2026-11-08', 4.80, 115000, 'KH003');

-- 10. LÔ NHẬP ĐỘNG VẬT
INSERT INTO LoNhap (MaLo, LoaiDongVat, SoLuongNhap, TongCanNangLucNhap, DonGiaNhap, MaPhieuNhap) VALUES
('LO001', 'Lợn thịt Yorkshire', 6, 90.00, 85000, 'PN001'),
('LO002', 'Bò thịt lai Sind', 3, 450.00, 110000, 'PN001'),
('LO003', 'Gà ri lai thả vườn', 4, 2.40, 120000, 'PN002');

-- 11. ĐỘNG VẬT (13 cá thể: 4 con đã xuất bán, 9 con đang nuôi)
INSERT INTO DongVat (MaDongVat, GioiTinh, CanNangHienTai, TinhTrangSucKhoe, GhiChu, MaChuong, MaLo, MaPhieuXuat) VALUES
('DV001', 'Đực', 106.50, 'Khỏe mạnh', 'Đạt chuẩn xuất chuồng đợt 1', 'CHA01', 'LO001', 'PX001'),
('DV002', 'Cái', 105.00, 'Khỏe mạnh', 'Đạt chuẩn xuất chuồng đợt 1', 'CHA01', 'LO001', 'PX001'),
('DV003', 'Đực', 88.50, 'Sức khỏe ổn định', 'Từng điều trị viêm phổi nhẹ, đã hồi phục', 'CHA01', 'LO001', NULL),
('DV004', 'Cái', 85.00, 'Tốt', 'Ăn khỏe, tăng trọng đều', 'CHA01', 'LO001', NULL),
('DV005', 'Đực', 82.50, 'Khỏe mạnh', 'Nuôi vỗ béo theo đàn', 'CHA02', 'LO001', NULL),
('DV006', 'Cái', 81.00, 'Khỏe mạnh', 'Tăng trưởng đúng biểu đồ', 'CHA02', 'LO001', NULL),
('DV007', 'Đực', 278.00, 'Tốt', 'Khung xương to, vỗ béo tốt', 'CHB01', 'LO002', NULL),
('DV008', 'Đực', 265.50, 'Khỏe mạnh', 'Đã tiêm phòng Lở mồm long móng', 'CHB01', 'LO002', NULL),
('DV009', 'Cái', 254.00, 'Khỏe mạnh', 'Ăn uống đúng khẩu phần', 'CHB01', 'LO002', NULL),
('DV010', 'Trống', 2.45, 'Khỏe mạnh', 'Gà thịt thương phẩm đạt chuẩn', 'CHC01', 'LO003', 'PX002'),
('DV011', 'Mái', 2.35, 'Khỏe mạnh', 'Gà thịt thương phẩm đạt chuẩn', 'CHC01', 'LO003', 'PX002'),
('DV012', 'Trống', 2.10, 'Ổn định', 'Đang nuôi thúc giai đoạn cuối', 'CHC01', 'LO003', NULL),
('DV013', 'Mái', 1.90, 'Khỏe mạnh', 'Lông mượt, phát triển đồng đều', 'CHC01', 'LO003', NULL);

-- 12. HỒ SƠ BỆNH ÁN (Thực thể yếu - Khóa chính MaDongVat + LanKham)
INSERT INTO HoSoBenhAn (MaDongVat, LanKham, NgayKham, ChanDoan, DonThuoc, MaNV) VALUES
('DV003', 1, '2026-08-12', 'Viêm phổi địa phương do thay đổi thời tiết', 'Tiêm Tylosin 20% (3 ngày) + Vitamin C pha nước', 'NV001'),
('DV003', 2, '2026-08-20', 'Tái khám: Hết ho, thân nhiệt ổn định, ăn tốt', 'Trộn men tiêu hóa Bio-lacto vào cám 5 ngày', 'NV001'),
('DV008', 1, '2026-09-15', 'Khám sàng lọc & tiêm phòng định kỳ', 'Tiêm vắc-xin Lở mồm long móng (2ml/con)', 'NV001'),
('DV012', 1, '2026-10-02', 'Rối loạn tiêu hóa nhẹ do thời tiết ẩm', 'Pha điện giải Oresol + men sống 3 ngày', 'NV002');

-- 13. CHĂM SÓC (Phân công nhân viên đang làm việc chăm sóc toàn bộ vật nuôi hiện có)
INSERT INTO ChamSoc (MaNV, MaDongVat) VALUES
('NV003', 'DV003'),
('NV003', 'DV004'),
('NV003', 'DV005'),
('NV003', 'DV006'),
('NV004', 'DV007'),
('NV004', 'DV008'),
('NV004', 'DV009'),
('NV004', 'DV012'),
('NV004', 'DV013');

-- 14. GỒM (Chi tiết nhập thức ăn theo phiếu nhập)
INSERT INTO Gom (MaPhieuNhap, MaThucAn, SoLuongNhap, DonGiaNhap) VALUES
('PN003', 'TA001', 40.00, 320000),
('PN003', 'TA004', 500.00, 7500),
('PN004', 'TA002', 30.00, 350000),
('PN004', 'TA003', 25.00, 290000);