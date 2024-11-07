package dataaccess;

import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTest {

    private static SQLGameDAO gameDAO;

    @BeforeAll
    static void init() {
        // Set up database connection for testing
        DatabaseManager.setTestConnection("jdbc:mysql://localhost/test_db", "test_user", "test_password");
        gameDAO = new SQLGameDAO();
    }

    @BeforeEach
    void setup() throws DataAccessException {
        // Clear data before each test
        gameDAO.clear();

        // Add initial test data
        ChessGame chessGame = new ChessGame();  // Assuming ChessGame has a default constructor
        gameDAO.createGame(new GameData(1, "player1", "player2", "Initial Game", chessGame));
    }

    @Test
    @DisplayName("Create Game - Positive Case")
    void createGameTestPositive() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData newGame = new GameData(2, "playerA", "playerB", "New Game", chessGame);

        gameDAO.createGame(newGame);

        GameData retrievedGame = gameDAO.getGame(2);
        assertNotNull(retrievedGame, "Game should be created successfully");
        assertEquals("New Game", retrievedGame.gameName(), "Game names should match");
        assertEquals("playerA", retrievedGame.whiteUsername(), "White player usernames should match");
        assertEquals("playerB", retrievedGame.blackUsername(), "Black player usernames should match");
    }

    @Test
    @DisplayName("Create Game with Duplicate ID - Negative Case")
    void createGameTestNegative() {
        ChessGame chessGame = new ChessGame();
        GameData duplicateGame = new GameData(1, "player1", "player2", "Duplicate Game", chessGame);

        assertThrows(DataAccessException.class, () -> gameDAO.createGame(duplicateGame),
                "Creating a game with duplicate ID should throw an exception");
    }

    @Test
    @DisplayName("Get Existing Game - Positive Case")
    void getGameTestPositive() throws DataAccessException {
        GameData retrievedGame = gameDAO.getGame(1);

        assertNotNull(retrievedGame, "Game should exist in the database");
        assertEquals("Initial Game", retrievedGame.gameName(), "Game names should match");
        assertEquals("player1", retrievedGame.whiteUsername(), "White player usernames should match");
        assertEquals("player2", retrievedGame.blackUsername(), "Black player usernames should match");
    }

    @Test
    @DisplayName("Get Non-Existent Game - Negative Case")
    void getGameTestNegative() {
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(999),
                "Getting a non-existent game should throw an exception");
    }

    @Test
    @DisplayName("Check Game Existence - Positive Case")
    void gameExistsTestPositive() {
        assertTrue(gameDAO.gameExists(1), "Game with ID 1 should exist");
    }

    @Test
    @DisplayName("Check Non-Existent Game - Negative Case")
    void gameExistsTestNegative() {
        assertFalse(gameDAO.gameExists(999), "Game with ID 999 should not exist");
    }

    @Test
    @DisplayName("Update Game - Positive Case")
    void updateGameTestPositive() throws DataAccessException {
        ChessGame updatedChessGame = new ChessGame();  // Assuming ChessGame has relevant modifications
        GameData updatedGame = new GameData(1, "newPlayer1", "newPlayer2", "Updated Game", updatedChessGame);

        gameDAO.updateGame(updatedGame);

        GameData retrievedGame = gameDAO.getGame(1);
        assertNotNull(retrievedGame, "Updated game should exist in the database");
        assertEquals("Updated Game", retrievedGame.gameName(), "Game names should match updated value");
        assertEquals("newPlayer1", retrievedGame.whiteUsername(), "White player usernames should match updated value");
        assertEquals("newPlayer2", retrievedGame.blackUsername(), "Black player usernames should match updated value");
    }

    @Test
    @DisplayName("Update Non-Existent Game - Negative Case")
    void updateGameTestNegative() {
        ChessGame chessGame = new ChessGame();
        GameData nonExistentGame = new GameData(999, "nonExistentUser1", "nonExistentUser2", "Non-Existent Game", chessGame);

        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(nonExistentGame),
                "Updating a non-existent game should throw an exception");
    }

    @Test
    @DisplayName("Clear Game Data - Positive Case")
    void clearTestPositive() throws DataAccessException {
        assertNotNull(gameDAO.getGame(1), "GameDAO should contain game data before clear");

        gameDAO.clear();

        assertThrows(DataAccessException.class, () -> gameDAO.getGame(1),
                "GameDAO should be empty after clear, attempting to get a game should throw an exception");
    }

    @AfterEach
    void tearDown() throws DataAccessException {
        gameDAO.clear();
    }
}
