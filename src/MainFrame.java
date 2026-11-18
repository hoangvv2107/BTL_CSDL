import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/** Cửa sổ chính, mỗi yêu cầu trong đề bài là một tab riêng. */
class MainFrame extends JFrame {
    MainFrame() {
        setTitle("Quản lý trang trại chăn nuôi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setLocationRelativeTo(null);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Quản lý lô nhập", new LoNhapPanel());
        tabs.addTab("2. Quản lý động vật", new DongVatPanel());
        tabs.addTab("3. Tìm kiếm động vật", new SearchPanel());
        tabs.addTab("4. Báo cáo chuồng", new ReportChuongPanel());
        tabs.addTab("5. Báo cáo doanh thu", new RevenuePanel());
        add(tabs);
    }
}
