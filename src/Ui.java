import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    /** Xuất báo cáo .xlsx có phần đầu trang, bảng dữ liệu và dòng tổng cộng. */
    static void exportExcel(Component parent, JTable table, String defaultName, String title, String subtitle) {
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
            int columns = table.getColumnCount();
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, columns - 1));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, columns - 1));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(2, 2, 0, columns - 1));

            CellStyle organizationStyle = workbook.createCellStyle();
            organizationStyle.setAlignment(HorizontalAlignment.CENTER);
            organizationStyle.setFont(font(workbook, true, 12, IndexedColors.DARK_BLUE.getIndex()));
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setFont(font(workbook, true, 16, IndexedColors.DARK_BLUE.getIndex()));
            CellStyle subtitleStyle = workbook.createCellStyle();
            subtitleStyle.setAlignment(HorizontalAlignment.CENTER);
            subtitleStyle.setFont(font(workbook, false, 11, IndexedColors.GREY_80_PERCENT.getIndex()));
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(font(workbook, true, 10, IndexedColors.DARK_BLUE.getIndex()));
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            CellStyle bodyStyle = workbook.createCellStyle();
            bodyStyle.setBorderBottom(BorderStyle.THIN);
            bodyStyle.setBorderTop(BorderStyle.THIN);
            bodyStyle.setBorderLeft(BorderStyle.THIN);
            bodyStyle.setBorderRight(BorderStyle.THIN);
            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.cloneStyleFrom(headerStyle);
            totalStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            totalStyle.setFont(font(workbook, true, 10, IndexedColors.DARK_BLUE.getIndex()));

            setCell(sheet.createRow(0).createCell(0), "TRANG TRẠI CHĂN NUÔI", organizationStyle);
            setCell(sheet.createRow(1).createCell(0), title, titleStyle);
            setCell(sheet.createRow(2).createCell(0), subtitle + " | Trích xuất: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), subtitleStyle);

            Row header = sheet.createRow(4);
            for (int c = 0; c < table.getColumnCount(); c++) {
                setCell(header.createCell(c), table.getColumnName(c), headerStyle);
            }
            for (int r = 0; r < table.getRowCount(); r++) {
                Row row = sheet.createRow(r + 5);
                for (int c = 0; c < table.getColumnCount(); c++) {
                    Object value = table.getValueAt(r, c);
                    Cell cell = row.createCell(c);
                    if (value instanceof Number number) cell.setCellValue(number.doubleValue());
                    else cell.setCellValue(value == null ? "" : String.valueOf(value));
                    cell.setCellStyle(bodyStyle);
                }
            }
            int totalRowIndex = table.getRowCount() + 5;
            Row total = sheet.createRow(totalRowIndex);
            setCell(total.createCell(0), "TỔNG CỘNG", totalStyle);
            if (columns > 3) {
                double count = 0;
                for (int r = 0; r < table.getRowCount(); r++) {
                    Object value = table.getValueAt(r, 3);
                    if (value instanceof Number number) count += number.doubleValue();
                }
                setNumberCell(total.createCell(3), count, totalStyle);
                if (columns > 6 && title.contains("DOANH THU")) {
                    double sum = 0;
                    for (int r = 0; r < table.getRowCount(); r++) {
                        Object value = table.getValueAt(r, columns - 1);
                        if (value instanceof Number number) sum += number.doubleValue();
                    }
                    setNumberCell(total.createCell(columns - 1), sum, totalStyle);
                }
            }
            for (int c = 1; c < columns; c++) {
                if (total.getCell(c) == null) total.createCell(c);
                total.getCell(c).setCellStyle(totalStyle);
            }
            for (int c = 0; c < columns; c++) {
                sheet.autoSizeColumn(c);
                sheet.setColumnWidth(c, Math.min(sheet.getColumnWidth(c) + 600, 15000));
            }
            sheet.createFreezePane(0, 5);
            workbook.write(out);
            info(parent, "Đã xuất báo cáo Excel .xlsx:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            error(parent, e);
        }
    }

    private static void setNumberCell(Cell cell, double value, CellStyle style) {
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static org.apache.poi.ss.usermodel.Font font(Workbook workbook, boolean bold, int size, short color) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints((short) size);
        font.setFontName("Arial");
        font.setColor(color);
        return font;
    }

    private static void setCell(Cell cell, String value, CellStyle style) {
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
