package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

public class SQLGameDAO implements GameDAO {

    private final Gson gson;

    public SQLGameDAO() {
        gson = new Gson();
        initializeDatabase();
    }

    // Initializes the database and creates the table if it doesn't exist
    private void initializeDatabase() {
        try {
            DatabaseManager.createDatabase();
            try (Connection conn = DatabaseManager.getConnection()) {
                String createTableSQL = """
                        CREATE TABLE IF NOT EXISTS game (
                            gameID INT NOT NULL,
                            whiteUsername VARCHAR(255),
                            blackUsername VARCHAR(255),
                            gameName VARCHAR(255),
                            chessGame TEXT,
                            PRIMARY KEY (gameID)
                        )""";
                try (PreparedStatement createTableStmt = conn.prepareStatement(createTableSQL)) {
                    createTableStmt.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    @Override
    public List<GameData> listGames() {
        HashSet<GameData> games = new HashSet<>();
        String query = "SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM game";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet results = stmt.executeQuery()) {

            while (results.next()) {
                int gameID = results.getInt("gameID");
                String whiteUsername = results.getString("whiteUsername");
                String blackUsername = results.getString("blackUsername");
                String gameName = results.getString("gameName");
                ChessGame chessGame = deserializeGame(results.getString("chessGame"));
                games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame));
            }
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        }
        return (List<GameData>) games;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        String insertSQL = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, chessGame) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            stmt.setInt(1, game.gameID());
            stmt.setString(2, game.whiteUsername());
            stmt.setString(3, game.blackUsername());
            stmt.setString(4, game.gameName());
            stmt.setString(5, serializeGame(game.game()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String query = "SELECT whiteUsername, blackUsername, gameName, chessGame FROM game WHERE gameID=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, gameID);
            try (ResultSet results = stmt.executeQuery()) {
                if (results.next()) {
                    String whiteUsername = results.getString("whiteUsername");
                    String blackUsername = results.getString("blackUsername");
                    String gameName = results.getString("gameName");
                    ChessGame chessGame = deserializeGame(results.getString("chessGame"));
                    return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                } else {
                    throw new DataAccessException("Game not found with ID: " + gameID);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + e.getMessage());
        }
    }

    @Override
    public boolean gameExists(int gameID) {
        String query = "SELECT gameID FROM game WHERE gameID=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, gameID);
            try (ResultSet results = stmt.executeQuery()) {
                return results.next();
            }
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String updateSQL = "UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, chessGame=? WHERE gameID=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSQL)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, serializeGame(game.game()));
            stmt.setInt(5, game.gameID());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DataAccessException("No game found with ID: " + game.gameID());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        String clearSQL = "TRUNCATE TABLE game";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(clearSQL)) {

            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        }
    }

    // Helper method to serialize a ChessGame object to JSON
    private String serializeGame(ChessGame game) {
        return gson.toJson(game);
    }

    // Helper method to deserialize JSON back to a ChessGame object
    private ChessGame deserializeGame(String serializedGame) {
        return gson.fromJson(serializedGame, ChessGame.class);
    }
}
