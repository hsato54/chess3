package dataaccess;

import model.AuthData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() {
        initializeDatabase();
    }

    // Initializes the database and creates the auth table if it does not exist
    private void initializeDatabase() {
        try {
            DatabaseManager.createDatabase();
            String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS auth (
                        username VARCHAR(255) NOT NULL,
                        authToken VARCHAR(255) NOT NULL,
                        PRIMARY KEY (authToken)
                    )""";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(createTableSQL)) {
                stmt.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Failed to initialize the auth table: " + e.getMessage(), e);
        }
    }

    @Override
    public void addAuth(AuthData authData) {
        try {
            createAuth(authData);
        } catch (DataAccessException e) {
            e.printStackTrace(); // Log error for debugging.
        }
    }

    @Override
    public void deleteAuth(String authToken) {
        String deleteSQL = "DELETE FROM auth WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {

            stmt.setString(1, authToken);
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace(); // Log error for debugging.
        }
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        String insertSQL = "INSERT INTO auth (username, authToken) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            stmt.setString(1, authData.username());
            stmt.setString(2, authData.authToken());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth entry: " + e.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String querySQL = "SELECT username FROM auth WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(querySQL)) {

            stmt.setString(1, authToken);
            try (ResultSet results = stmt.executeQuery()) {
                if (results.next()) {
                    String username = results.getString("username");
                    return new AuthData(username, authToken);
                } else {
                    throw new DataAccessException("Auth token not found: " + authToken);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching auth data: " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        String truncateSQL = "TRUNCATE TABLE auth";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(truncateSQL)) {

            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace(); // Log error for debugging.
        }
    }
}
