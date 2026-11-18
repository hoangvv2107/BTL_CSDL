import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Màn hình tìm kiếm động vật theo mã/ghi chú và các bộ lọc. */
class SearchPanel extends JPanel {
    private final JTextField keyword = Ui.field(18), loai = Ui.field(14);
    private final JComboBox<String> chuong = new JComboBox<>();
    private final JComboBox<String> status = new JComboBox<>(new String[]{"Tất cả", "Đang nuôi", "Đã bán"});
    private final DefaultTableModel model = Ui.model("Mã", "Giới tính", "Cân nặng", "Ghi chú", "Chuồng", "Loài", "Trạng thái");

    SearchPanel() {
        setLayout(new BorderLayout());
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("Mã/Ghi chú:")); filters.add(keyword); filters.add(new JLabel("Chuồng:")); filters.add(chuong);
        filters.add(new JLabel("Loại động vật:")); filters.add(loai); filters.add(new JLabel("Trạng thái:")); filters.add(status);
        JButton search = new JButton("Tìm kiếm"), clear = new JButton("Xóa bộ lọc");
        filters.add(search); filters.add(clear); add(filters, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        search.addActionListener(e -> search());
        clear.addActionListener(e -> { keyword.setText(""); loai.setText(""); status.setSelectedIndex(0); chuong.setSelectedIndex(0); search(); });
        loadChuong(); search();
    }

    private void loadChuong() {
        chuong.addItem("Tất cả");
        try (Connection c = Database.open(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT MaChuong FROM Chuong ORDER BY MaChuong")) {
            while (r.next()) chuong.addItem(r.getString(1));
        } catch (Exception e) { Ui.error(this, e); }
    }

    private void search() {
        model.setRowCount(0);
        StringBuilder sql = new StringBuilder("SELECT d.*, l.LoaiDongVat FROM DongVat d JOIN LoNhap l ON d.MaLo=l.MaLo WHERE (d.MaDongVat LIKE ? OR COALESCE(d.GhiChu,'') LIKE ?)");
        List<Object> params = new ArrayList<>();
        String key = "%" + keyword.getText().trim() + "%"; params.add(key); params.add(key);
        if (!"Tất cả".equals(chuong.getSelectedItem())) { sql.append(" AND d.MaChuong=?"); params.add(chuong.getSelectedItem()); }
        if (!loai.getText().trim().isEmpty()) { sql.append(" AND l.LoaiDongVat LIKE ?"); params.add("%" + loai.getText().trim() + "%"); }
        if (!"Tất cả".equals(status.getSelectedItem())) {
            sql.append(" Đã bán".equals(status.getSelectedItem()) ? " AND d.MaPhieuXuat IS NOT NULL" : " AND d.MaPhieuXuat IS NULL");
        }
        sql.append(" ORDER BY d.MaDongVat");
        try (Connection c = Database.open(); PreparedStatement p = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) p.setObject(i + 1, params.get(i));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) model.addRow(new Object[]{r.getString("MaDongVat"), r.getString("GioiTinh"), r.getBigDecimal("CanNangHienTai"), r.getString("GhiChu"), r.getString("MaChuong"), r.getString("LoaiDongVat"), r.getString("MaPhieuXuat") == null ? "Đang nuôi" : "Đã bán"});
            }
        } catch (Exception e) { Ui.error(this, e); }
    }
}
