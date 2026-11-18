import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

/** Form 2 - Quản lý từng cá thể động vật và thông tin xuất bán. */
class DongVatPanel extends JPanel {
    private final JTextField ma = Ui.field(10), canNang = Ui.field(10), sucKhoe = Ui.field(18), ghiChu = Ui.field(20);
    private final JComboBox<String> maChuong = new JComboBox<>(), maLo = new JComboBox<>(), maPhieuXuat = new JComboBox<>();
    private final JComboBox<String> gioiTinh = new JComboBox<>(new String[]{"Đực", "Cái", "Trống", "Mái"});
    private final DefaultTableModel model = Ui.model("Mã", "Giới tính", "Cân nặng", "Sức khỏe", "Ghi chú", "Mã chuồng", "Mã lô", "Mã phiếu xuất");
    private final JTable table = new JTable(model);

    DongVatPanel() {
        setLayout(new BorderLayout()); JPanel form = Ui.form();
        Ui.addRow(form, 0, "Mã động vật:", ma); Ui.addRow(form, 1, "Giới tính:", gioiTinh);
        Ui.addRow(form, 2, "Cân nặng hiện tại:", canNang); Ui.addRow(form, 3, "Tình trạng sức khỏe:", sucKhoe);
        Ui.addRow(form, 4, "Ghi chú:", ghiChu); Ui.addRow(form, 5, "Mã chuồng:", maChuong);
        Ui.addRow(form, 6, "Mã lô:", maLo); Ui.addRow(form, 7, "Mã phiếu xuất (có thể trống):", maPhieuXuat);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Thêm"), update = new JButton("Sửa"), delete = new JButton("Xóa"), reload = new JButton("Tải lại");
        buttons.add(add); buttons.add(update); buttons.add(delete); buttons.add(reload);
        form.add(buttons, new GridBagConstraints(0, 8, 2, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
        add(form, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(e -> fillFromTable());
        add.addActionListener(e -> save(false)); update.addActionListener(e -> save(true));
        delete.addActionListener(e -> remove()); reload.addActionListener(e -> { loadCombos(); load(); });
        loadCombos(); load();
    }
    private void fillFromTable() {
        int r = table.getSelectedRow(); if (r < 0) return;
        ma.setText(String.valueOf(model.getValueAt(r, 0))); gioiTinh.setSelectedItem(model.getValueAt(r, 1));
        canNang.setText(String.valueOf(model.getValueAt(r, 2))); sucKhoe.setText(String.valueOf(model.getValueAt(r, 3)));
        ghiChu.setText(String.valueOf(model.getValueAt(r, 4))); maChuong.setSelectedItem(String.valueOf(model.getValueAt(r, 5)));
        maLo.setSelectedItem(String.valueOf(model.getValueAt(r, 6))); maPhieuXuat.setSelectedItem(String.valueOf(model.getValueAt(r, 7)));
        if ("null".equals(String.valueOf(model.getValueAt(r, 7)))) maPhieuXuat.setSelectedIndex(0);
    }
    private void save(boolean update) {
        String maValue = ma.getText().trim();
        String chuongValue = String.valueOf(maChuong.getSelectedItem());
        String loValue = String.valueOf(maLo.getSelectedItem());
        String phieuValue = maPhieuXuat.getSelectedIndex() == 0 ? null : String.valueOf(maPhieuXuat.getSelectedItem());
        String sucKhoeValue = sucKhoe.getText().trim();
        if (maValue.isEmpty() || canNang.getText().trim().isEmpty()
                || "null".equals(chuongValue) || "null".equals(loValue)) {
            Ui.error(this, new Exception("Vui lòng nhập đầy đủ mã, cân nặng, mã chuồng và mã lô."));
            return;
        }
        if (!maValue.matches("[A-Za-z]{2}\\d{3}")) {
            Ui.error(this, new Exception("Mã động vật phải có dạng 2 chữ cái và 3 chữ số, ví dụ: DV014."));
            return;
        }
        if (sucKhoeValue.length() > 100) {
            Ui.error(this, new Exception("Tình trạng sức khỏe không được dài quá 100 ký tự."));
            return;
        }
        BigDecimal canNangValue;
        try {
            canNangValue = new BigDecimal(canNang.getText().trim());
        } catch (NumberFormatException e) {
            Ui.error(this, new Exception("Cân nặng phải là một số hợp lệ."));
            return;
        }
        if (canNangValue.signum() <= 0) {
            Ui.error(this, new Exception("Cân nặng hiện tại phải lớn hơn 0."));
            return;
        }
        String sql = update ? "UPDATE DongVat SET GioiTinh=?,CanNangHienTai=?,TinhTrangSucKhoe=?,GhiChu=?,MaChuong=?,MaLo=?,MaPhieuXuat=? WHERE MaDongVat=?"
                : "INSERT INTO DongVat(MaDongVat,GioiTinh,CanNangHienTai,TinhTrangSucKhoe,GhiChu,MaChuong,MaLo,MaPhieuXuat) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = Database.open(); PreparedStatement p = c.prepareStatement(sql)) {
            String duplicateSql = update
                    ? "SELECT COUNT(*) FROM DongVat WHERE MaDongVat=? AND MaDongVat<>?"
                    : "SELECT COUNT(*) FROM DongVat WHERE MaDongVat=?";
            try (PreparedStatement duplicate = c.prepareStatement(duplicateSql)) {
                duplicate.setString(1, maValue);
                if (update) duplicate.setString(2, maValue);
                try (ResultSet result = duplicate.executeQuery()) {
                    result.next();
                    if (result.getInt(1) > 0) {
                        Ui.error(this, new Exception("Mã động vật đã tồn tại, vui lòng nhập mã khác."));
                        return;
                    }
                }
            }
            if (!update || phieuValue == null) {
                String capacitySql = "SELECT c.SucChua, COUNT(d.MaDongVat) FROM Chuong c " +
                        "LEFT JOIN DongVat d ON d.MaChuong=c.MaChuong AND d.MaPhieuXuat IS NULL " +
                        (update ? "AND d.MaDongVat<>? " : "") +
                        "WHERE c.MaChuong=? GROUP BY c.SucChua";
                try (PreparedStatement capacity = c.prepareStatement(capacitySql)) {
                    if (update) {
                        capacity.setString(1, maValue);
                        capacity.setString(2, chuongValue);
                    } else {
                        capacity.setString(1, chuongValue);
                    }
                    try (ResultSet result = capacity.executeQuery()) {
                        if (result.next() && result.getInt(2) >= result.getInt(1)) {
                            Ui.error(this, new Exception("Chuồng đã đủ sức chứa, không thể thêm động vật đang nuôi."));
                            return;
                        }
                    }
                }
            }
            p.setString(1, String.valueOf(gioiTinh.getSelectedItem())); p.setBigDecimal(2, canNangValue);
            p.setString(3, sucKhoeValue); p.setString(4, ghiChu.getText().trim()); p.setString(5, chuongValue);
            p.setString(6, loValue); if (phieuValue == null) p.setNull(7, Types.CHAR); else p.setString(7, phieuValue);
            p.setString(8, maValue); p.executeUpdate(); Ui.info(this, "Đã lưu dữ liệu động vật."); load();
        } catch (Exception e) { Ui.error(this, e); }
    }
    private void remove() {
        try (Connection c = Database.open(); PreparedStatement p = c.prepareStatement("DELETE FROM DongVat WHERE MaDongVat=?")) {
            p.setString(1, ma.getText().trim()); p.executeUpdate(); load();
        } catch (Exception e) { Ui.error(this, e); }
    }
    private void load() {
        model.setRowCount(0);
        try (Connection c = Database.open(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM DongVat ORDER BY MaDongVat")) {
            while (r.next()) model.addRow(new Object[]{r.getString("MaDongVat"), r.getString("GioiTinh"), r.getBigDecimal("CanNangHienTai"), r.getString("TinhTrangSucKhoe"), r.getString("GhiChu"), r.getString("MaChuong"), r.getString("MaLo"), r.getString("MaPhieuXuat")});
        } catch (Exception e) { Ui.error(this, e); }
    }
    // Nạp các danh mục khóa ngoại từ CSDL để người dùng chọn thay vì nhập mã.
    private void loadCombos() {
        maChuong.removeAllItems(); maLo.removeAllItems(); maPhieuXuat.removeAllItems(); maPhieuXuat.addItem("");
        try (Connection c = Database.open()) {
            addItems(c, maChuong, "SELECT MaChuong FROM Chuong ORDER BY MaChuong");
            addItems(c, maLo, "SELECT MaLo FROM LoNhap ORDER BY MaLo");
            addItems(c, maPhieuXuat, "SELECT MaPhieuXuat FROM PhieuXuatBan ORDER BY MaPhieuXuat");
        } catch (Exception e) { Ui.error(this, e); }
    }
    private void addItems(Connection c, JComboBox<String> combo, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { while (r.next()) combo.addItem(r.getString(1)); }
    }
}
