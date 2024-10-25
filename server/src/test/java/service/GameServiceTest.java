package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import service.*;


import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private static GameService gameService;
    private static GameDAO gameDAO;
    private static AuthDAO authDAO;

    private static AuthData authData;

    @BeforeAll
    static void init() {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);

        authData = new AuthData("Username", "authToken");

        authDAO.addAuth(authData);  // Add valid auth data for tests
    }

    @BeforeEach
    void setup() {
        gameDAO.clear(); // Clear games before each test
    }

    @Test
    @DisplayName("Create Valid Game")
    void createGameTestPositive() throws UnauthorizedException, BadRequestException, DataAccessException {
        int gameID1 = gameService.createGame(authData.authToken(), "Game1");
        assertTrue(gameDAO.gameExists(gameID1), "Game should exist after creation");

        int gameID2 = gameService.createGame(authData.authToken(), "Game2");
        assertNotEquals(gameID1, gameID2, "Each created game should have a unique ID");
    }

    @Test
    @DisplayName("Create Game with Invalid Token")
    void createGameTestNegative() {
        assertThrows(UnauthorizedException.class, () -> gameService.createGame("invalidToken", "GameName"),
                "Creating a game with an invalid token should throw UnauthorizedException");
    }

    @Test
    @DisplayName("List All Games with Valid Token")
    void listGamesTestPositive() throws UnauthorizedException, BadRequestException, DataAccessException {
        int gameID1 = gameService.createGame(authData.authToken(), "Game1");
        int gameID2 = gameService.createGame(authData.authToken(), "Game2");
        int gameID3 = gameService.createGame(authData.authToken(), "Game3");

        List<GameData> games = gameService.listGames(authData.authToken());

        assertEquals(3, games.size(), "There should be exactly three games listed");
        assertTrue(games.stream().anyMatch(game -> game.gameID() == gameID1), "Game1 should be in the list");
        assertTrue(games.stream().anyMatch(game -> game.gameID() == gameID2), "Game2 should be in the list");
        assertTrue(games.stream().anyMatch(game -> game.gameID() == gameID3), "Game3 should be in the list");
    }

    @Test
    @DisplayName("List Games with Invalid Token")
    void listGamesTestNegative() {
        assertThrows(UnauthorizedException.class, () -> gameService.listGames("invalidToken"),
                "Listing games with an invalid token should throw UnauthorizedException");
    }

    @Test
    @DisplayName("Join Game Successfully")
    void joinGameTestPositive() throws UnauthorizedException, BadRequestException, DataAccessException {
        int gameID = gameService.createGame(authData.authToken(), "JoinableGame");

        assertTrue(gameService.joinGame(authData.authToken(), gameID, "WHITE"),
                "Joining the game as WHITE should be successful");

        GameData gameData = gameDAO.getGame(gameID);
        assertEquals(authData.username(), gameData.whiteUsername(), "Username should match the WHITE player in the game data");
    }

    @Test
    @DisplayName("Join Game with Invalid Conditions")
    void joinGameTestNegative() throws UnauthorizedException, BadRequestException, DataAccessException {
        int gameID = gameService.createGame(authData.authToken(), "GameWithConstraints");

        // Try with an invalid token
        assertThrows(UnauthorizedException.class, () -> gameService.joinGame("invalidToken", gameID, "WHITE"),
                "Joining with an invalid token should throw UnauthorizedException");

        // Try joining a non-existent game
        assertThrows(BadRequestException.class, () -> gameService.joinGame(authData.authToken(), 99999, "WHITE"),
                "Joining a non-existent game should throw BadRequestException");

        // Try joining with an invalid color
        assertThrows(BadRequestException.class, () -> gameService.joinGame(authData.authToken(), gameID, "INVALID_COLOR"),
                "Joining with an invalid color should throw BadRequestException");
    }

    @Test
    @DisplayName("Clear All Games from Database")
    void clearTestPositive() throws UnauthorizedException, BadRequestException, DataAccessException {
        gameService.createGame(authData.authToken(), "GameToClear");
        gameService.clear();

        assertEquals(0, gameDAO.listGames().size(), "All games should be cleared from the database");
    }

    @Test
    @DisplayName("Clear Database with No Errors")
    void clearTestWithoutExceptions() {
        assertDoesNotThrow(() -> gameService.clear(),
                "Clearing the database should not throw any exceptions even if it is already empty");
    }
}
