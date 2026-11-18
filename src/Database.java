import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Quản lý kết nối MySQL cho toàn bộ ứng dụng. */
final class Database {
    private Database() {}

    static Connection open() throws SQLException {
        String url = setting("DB_URL", "jdbc:mysql://localhost:3306/Trangtrai?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh");
        String user = setting("DB_USER", "root");
        String password = setting("DB_PASSWORD", "21072006");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Chưa thêm MySQL Connector/J vào classpath.", e);
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String setting(String name, String fallback) {
        return System.getProperty(name, System.getenv().getOrDefault(name, fallback));
    }
}
