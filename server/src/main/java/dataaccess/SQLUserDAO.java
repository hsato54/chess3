package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;



public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException ex) {
            throw new RuntimeException(ex);
        }

        try (Connection conn = DatabaseManager.getConnection()) {
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
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

            stmt.setString(1, user.username());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.email());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user into database.");
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
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
//x
//    @Override
//    public void deleteUser(String username){
//        String sql = "DELETE FROM users WHERE username = ?";
//
//        try (Connection conn = Database.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setString(1, username);
//            stmt.executeUpdate();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public void clear() {
        String sql = "DELETE FROM users";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

}