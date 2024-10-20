

package dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL = "URL";
    private static final String USER = "user";
    private static final String PASSWORD = "password";

    public static void createDatabase() throws DataAccessException {
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new SQLException("Error connecting to the database", e);
        }
    }
}
