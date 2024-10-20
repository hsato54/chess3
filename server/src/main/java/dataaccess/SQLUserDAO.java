package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() {
        try {
            Database.createDatabase();
        } catch (DataAccessException ex) {
            throw new RuntimeException(ex);
        }

        try (Connection conn = Database.getConnection()) {
            String createUsersTableSQL = """
                    CREATE TABLE IF NOT EXISTS users (
                        username VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(255),
                        PRIMARY KEY (username)
                    )""";

            try (PreparedStatement createTableStatement = conn.prepareStatement(createUsersTableSQL)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT into users";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user into database.");
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserData(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            } else {
                throw new DataAccessException("User not found.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user from database.");
        }
    }
    @Override
    public void updateUser(UserData user){
        String sql = "UPDATE users SET password = ?, email = ? WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.password());
            stmt.setString(2, user.email());
            stmt.setString(3, user.username());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void deleteUser(String username){
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM users";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}