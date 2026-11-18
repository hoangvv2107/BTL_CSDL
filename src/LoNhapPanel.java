import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

/** Form 1 - Quản lý lô nhập: thêm, sửa, xóa các lô con giống. */
class LoNhapPanel extends JPanel {
    private final JTextField maLo = Ui.field(10), loai = Ui.field(18), soLuong = Ui.field(10);
    private final JTextField tongCan = Ui.field(10), donGia = Ui.field(10);
    private final JComboBox<String> maPhieu = new JComboBox<>();
    private final DefaultTableModel model = Ui.model("Mã lô", "Loài động vật", "Số lượng", "Tổng cân nặng", "Đơn giá", "Mã phiếu nhập");
    private final JTable table = new JTable(model);

    LoNhapPanel() {
        setLayout(new BorderLayout()); JPanel form = Ui.form();
        Ui.addRow(form, 0, "Mã lô:", maLo); Ui.addRow(form, 1, "Loài động vật:", loai);
        Ui.addRow(form, 2, "Số lượng nhập:", soLuong); Ui.addRow(form, 3, "Tổng cân nặng:", tongCan);
        Ui.addRow(form, 4, "Đơn giá nhập:", donGia); Ui.addRow(form, 5, "Mã phiếu nhập:", maPhieu);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Thêm"), update = new JButton("Sửa"), delete = new JButton("Xóa"), reload = new JButton("Tải lại");
        buttons.add(add); buttons.add(update); buttons.add(delete); buttons.add(reload);
        form.add(buttons, new GridBagConstraints(0, 6, 2, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
        add(form, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(e -> fillFromTable());
        add.addActionListener(e -> save(false)); update.addActionListener(e -> save(true));
        delete.addActionListener(e -> remove()); reload.addActionListener(e -> { loadCombo(); load(); });
        loadCombo(); load();
    }

    private void fillFromTable() {
        int r = table.getSelectedRow(); if (r < 0) return;
        maLo.setText(String.valueOf(model.getValueAt(r, 0))); loai.setText(String.valueOf(model.getValueAt(r, 1)));
        soLuong.setText(String.valueOf(model.getValueAt(r, 2))); tongCan.setText(String.valueOf(model.getValueAt(r, 3)));
        donGia.setText(String.valueOf(model.getValueAt(r, 4))); maPhieu.setSelectedItem(String.valueOf(model.getValueAt(r, 5)));
    }

    private void save(boolean update) {
        String sql = update ? "UPDATE LoNhap SET LoaiDongVat=?,SoLuongNhap=?,TongCanNangLucNhap=?,DonGiaNhap=?,MaPhieuNhap=? WHERE MaLo=?"
                : "INSERT INTO LoNhap(MaLo,LoaiDongVat,SoLuongNhap,TongCanNangLucNhap,DonGiaNhap,MaPhieuNhap) VALUES(?,?,?,?,?,?)";
        try (Connection c = Database.open(); PreparedStatement p = c.prepareStatement(sql)) {
            if (update) {
                p.setString(1, loai.getText().trim());
                p.setInt(2, Integer.parseInt(soLuong.getText().trim()));
                p.setBigDecimal(3, new BigDecimal(tongCan.getText().trim()));
                p.setBigDecimal(4, new BigDecimal(donGia.getText().trim()));
                p.setString(5, String.valueOf(maPhieu.getSelectedItem()));
                p.setString(6, maLo.getText().trim());
            } else {
                // Lệnh INSERT có MaLo ở vị trí đầu tiên, khác với lệnh UPDATE.
                p.setString(1, maLo.getText().trim());
                p.setString(2, loai.getText().trim());
                p.setInt(3, Integer.parseInt(soLuong.getText().trim()));
                p.setBigDecimal(4, new BigDecimal(tongCan.getText().trim()));
                p.setBigDecimal(5, new BigDecimal(donGia.getText().trim()));
                p.setString(6, String.valueOf(maPhieu.getSelectedItem()));
            }
            p.executeUpdate();
            Ui.info(this, update ? "Đã cập nhật lô nhập." : "Đã thêm lô nhập."); load();
        } catch (Exception e) { Ui.error(this, e); }
    }
    private void remove() {
        if (maLo.getText().trim().isEmpty()) return;
        int answer = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa lô " + maLo.getText().trim() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        try (Connection c = Database.open(); PreparedStatement p = c.prepareStatement("DELETE FROM LoNhap WHERE MaLo=?")) {
            p.setString(1, maLo.getText().trim()); p.executeUpdate(); load();
        } catch (SQLIntegrityConstraintViolationException e) {
            Ui.error(this, new Exception("Không thể xóa lô này vì đang có động vật tham chiếu.\n"
                    + "Hãy xóa hoặc chuyển các động vật thuộc lô trước."));
        } catch (Exception e) { Ui.error(this, e); }
    }
    private void load() {
        model.setRowCount(0);
        try (Connection c = Database.open(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM LoNhap ORDER BY MaLo")) {
            while (r.next()) model.addRow(new Object[]{r.getString("MaLo"), r.getString("LoaiDongVat"), r.getInt("SoLuongNhap"), r.getBigDecimal("TongCanNangLucNhap"), r.getBigDecimal("DonGiaNhap"), r.getString("MaPhieuNhap")});
        } catch (Exception e) { Ui.error(this, e); }
    }
    // Chỉ cho chọn phiếu nhập loại con giống, đúng ràng buộc nghiệp vụ.
    private void loadCombo() {
        maPhieu.removeAllItems();
        try (Connection c = Database.open(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT MaPhieuNhap FROM PhieuNhap WHERE LoaiPhieuNhap='Con giống' ORDER BY MaPhieuNhap")) {
            while (r.next()) maPhieu.addItem(r.getString(1));
        } catch (Exception e) { Ui.error(this, e); }
    }
}
