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
        String sql = update ? "UPDATE DongVat SET GioiTinh=?,CanNangHienTai=?,TinhTrangSucKhoe=?,GhiChu=?,MaChuong=?,MaLo=?,MaPhieuXuat=? WHERE MaDongVat=?"
                : "INSERT INTO DongVat(MaDongVat,GioiTinh,CanNangHienTai,TinhTrangSucKhoe,GhiChu,MaChuong,MaLo,MaPhieuXuat) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = Database.open(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, String.valueOf(gioiTinh.getSelectedItem())); p.setBigDecimal(2, new BigDecimal(canNang.getText().trim()));
            p.setString(3, sucKhoe.getText().trim()); p.setString(4, ghiChu.getText().trim()); p.setString(5, String.valueOf(maChuong.getSelectedItem()));
            p.setString(6, String.valueOf(maLo.getSelectedItem()));
            if (maPhieuXuat.getSelectedIndex() == 0) p.setNull(7, Types.CHAR); else p.setString(7, String.valueOf(maPhieuXuat.getSelectedItem()));
            p.setString(8, ma.getText().trim()); p.executeUpdate(); Ui.info(this, "Đã lưu dữ liệu động vật."); load();
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
