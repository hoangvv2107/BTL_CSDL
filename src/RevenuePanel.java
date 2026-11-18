import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

/** Báo cáo 2 - doanh thu xuất bán theo khoảng ngày. */
class RevenuePanel extends JPanel {
    private final JTextField from = Ui.field(10), to = Ui.field(10);
    private final DefaultTableModel model = Ui.model("Mã phiếu", "Ngày xuất", "Khách hàng", "Số con", "Tổng cân nặng", "Đơn giá bán", "Tổng doanh thu");

    RevenuePanel() {
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Từ ngày (yyyy-MM-dd):")); top.add(from); top.add(new JLabel("Đến ngày:")); top.add(to);
        JButton report = new JButton("Lập báo cáo"), export = new JButton("Xuất Excel");
        top.add(report); top.add(export);
        add(top, BorderLayout.NORTH); add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        report.addActionListener(e -> load());
        JTable table = (JTable) ((JScrollPane) getComponent(1)).getViewport().getView();
        export.addActionListener(e -> Ui.exportExcel(this, table, "BaoCaoDoanhThu"));
        from.setText(LocalDate.now().withDayOfMonth(1).toString()); to.setText(LocalDate.now().toString());
    }

    private void load() {
        model.setRowCount(0);
        String sql = "SELECT p.MaPhieuXuat,p.NgayXuat,k.TenKH,COUNT(d.MaDongVat) SoCon,p.TongCanNangLucXuat,p.DonGiaBan,(p.TongCanNangLucXuat*p.DonGiaBan) DoanhThu " +
                "FROM PhieuXuatBan p JOIN KhachHang k ON p.MaKH=k.MaKH LEFT JOIN DongVat d ON p.MaPhieuXuat=d.MaPhieuXuat " +
                "WHERE p.NgayXuat BETWEEN ? AND ? GROUP BY p.MaPhieuXuat,p.NgayXuat,k.TenKH,p.TongCanNangLucXuat,p.DonGiaBan ORDER BY p.NgayXuat";
        try (Connection c = Database.open(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, Date.valueOf(Ui.date(from.getText()))); p.setDate(2, Date.valueOf(Ui.date(to.getText())));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) model.addRow(new Object[]{r.getString(1), r.getDate(2), r.getString(3), r.getInt(4), r.getBigDecimal(5), r.getBigDecimal(6), r.getBigDecimal(7)});
            }
        } catch (Exception e) { Ui.error(this, e); }
    }
}
