import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** Báo cáo 1 - thống kê vật nuôi, cân nặng và chỗ trống theo chuồng. */
class ReportChuongPanel extends JPanel {
    private final DefaultTableModel model = Ui.model("Mã chuồng", "Tên chuồng", "Sức chứa", "Số con đang nuôi", "Tổng cân nặng", "Chỗ trống");

    ReportChuongPanel() {
        setLayout(new BorderLayout());
        JButton reload = new JButton("Tạo báo cáo");
        JButton export = new JButton("Xuất Excel");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT)); top.add(reload);
        top.add(export);
        add(top, BorderLayout.NORTH); add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        JTable table = (JTable) ((JScrollPane) getComponent(1)).getViewport().getView();
        reload.addActionListener(e -> load());
        export.addActionListener(e -> Ui.exportExcel(this, table, "BaoCaoThongKeTheoChuong"));
        load();
    }

    private void load() {
        model.setRowCount(0);
        String sql = "SELECT c.MaChuong,c.TenChuong,c.SucChua,COUNT(d.MaDongVat) SoCon,COALESCE(SUM(d.CanNangHienTai),0) TongCan,(c.SucChua-COUNT(d.MaDongVat)) ChoTrong " +
                "FROM Chuong c LEFT JOIN DongVat d ON c.MaChuong=d.MaChuong AND d.MaPhieuXuat IS NULL " +
                "GROUP BY c.MaChuong,c.TenChuong,c.SucChua ORDER BY c.MaChuong";
        try (Connection c = Database.open(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) model.addRow(new Object[]{r.getString(1), r.getString(2), r.getInt(3), r.getInt(4), r.getBigDecimal(5), r.getInt(6)});
        } catch (Exception e) { Ui.error(this, e); }
    }
}
