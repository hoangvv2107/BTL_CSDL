import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;

/** Các thành phần giao diện dùng chung, giúp các màn hình nhất quán. */
final class Ui {
    private Ui() {}
    static JTextField field(int columns) { return new JTextField(columns); }
    static JPanel form() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return p;
    }
    static void addRow(JPanel p, int row, String label, Component component) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0; left.gridy = row; left.anchor = GridBagConstraints.WEST;
        left.insets = new Insets(4, 4, 4, 10); p.add(new JLabel(label), left);
        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1; right.gridy = row; right.weightx = 1; right.fill = GridBagConstraints.HORIZONTAL;
        right.insets = new Insets(4, 4, 4, 4); p.add(component, right);
    }
    static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }
    static void error(Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
    static LocalDate date(String value) { return LocalDate.parse(value.trim()); }

    /** Xuất nội dung bảng thành file Excel .xlsx thật bằng Apache POI. */
    static void exportExcel(Component parent, JTable table, String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName + ".xlsx"));
        chooser.setDialogTitle("Chọn nơi lưu file Excel");
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("Báo cáo");
            Row header = sheet.createRow(0);
            for (int c = 0; c < table.getColumnCount(); c++) {
                header.createCell(c).setCellValue(table.getColumnName(c));
            }
            for (int r = 0; r < table.getRowCount(); r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < table.getColumnCount(); c++) {
                    Object value = table.getValueAt(r, c);
                    Cell cell = row.createCell(c);
                    if (value instanceof Number number) cell.setCellValue(number.doubleValue());
                    else cell.setCellValue(value == null ? "" : String.valueOf(value));
                }
            }
            for (int c = 0; c < table.getColumnCount(); c++) sheet.autoSizeColumn(c);
            workbook.write(out);
            info(parent, "Đã xuất file Excel .xlsx:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            error(parent, e);
        }
    }
}
