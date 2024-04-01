package chat;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseUtil {

    public static Connection getConnection() throws Exception {
        Properties prop = new Properties();

        // 프로퍼티 파일 로드
        try (InputStream input = DatabaseUtil.class.getClassLoader().getResourceAsStream("database.properties")) {
            prop.load(input);
        }

        // 프로퍼티를 통해 데이터베이스 연결 정보 가져오기
        String dbURL = prop.getProperty("db.url");
        String dbUser = prop.getProperty("db.user");
        String dbPassword = prop.getProperty("db.password");

        // 데이터베이스 연결
        Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);
        return conn;
    }
}
