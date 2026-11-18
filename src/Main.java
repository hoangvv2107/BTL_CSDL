import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Điểm khởi động ứng dụng. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Dùng giao diện mặc định nếu hệ điều hành không hỗ trợ.
            }
            new MainFrame().setVisible(true);
        });
    }
}
