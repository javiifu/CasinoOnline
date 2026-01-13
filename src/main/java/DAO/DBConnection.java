package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = System.getenv("CASINO_DB_URL");
    private static final String USER = System.getenv("CASINO_DB_USER");
    private static final String PASSWORD = System.getenv("CASINO_DB_PASSWORD");

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (URL == null || USER == null || PASSWORD == null) {
            throw new SQLException("Credenciales de BD no configuradas en variables de entorno.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
