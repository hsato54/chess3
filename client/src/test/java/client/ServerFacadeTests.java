
import org.junit.jupiter.api.*;
import server.Server;
import ui.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on port " + port);
        facade = new ServerFacade("localhost:" + port);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setup() {
        facade.logout();
        facade.clear();
    }
    @Test
    public void testLoginSuccess() {
        facade.register("player2", "password", "p2@email.com");
        boolean success = facade.login("player2", "password");
        assertTrue(success, "Login should succeed for valid credentials");
        assertNotNull(facade.getAuthToken(), "Auth token should be set after successful login");
    }

    @Test
    public void testLoginFail() {
        boolean success = facade.login("nonexistent", "password");
        assertFalse(success, "Login should fail for invalid credentials");
    }

    @Test
    public void testRegisterSuccess() {
        boolean success = facade.register("player1", "password", "p1@email.com");
        assertTrue(success, "Registration should succeed");
        assertNotNull(facade.getAuthToken(), "Auth token should be set after successful registration");
    }

    @Test
    public void testRegisterFail() {
        boolean temp = facade.register("player1", "password", "p1@email.com");
        boolean success = facade.register("player1", "password", "p1@email.com");  // Registering same user again
        assertFalse(success, "Registration should fail for duplicate user");
    }

    @Test
    public void testLogout() {
        boolean temp2 = facade.register("player7", "password", "p7@email.com");
        boolean temp3 = facade.login("player7", "password");

        boolean success = facade.logout();
        assertTrue(success, "Logout should succeed for a logged-in user");
        assertNull(facade.getAuthToken(), "Auth token should be cleared after logout");
    }
    @Test
    public void testLogoutFailWhenNotLoggedIn() {
        facade.logout();

        boolean result = facade.logout();
        assertFalse(result, "Logout should fail when there is no user logged in");
    }

    @Test
    public void testCreateGameSuccess() {
        facade.register("player1", "password", "p1@email.com");
        facade.login("player1", "password");
        int gameId = facade.createGame("NewGame");
        assertNotEquals(-1, gameId, "Game ID should be valid if game creation is successful");
    }

    @Test
    public void testCreateGameFailWithoutLogin() {
        int gameId = facade.createGame("UnauthenticatedGame");
        assertEquals(-1, gameId, "Game creation should fail if the user is not logged in");
    }


    @Test
    public void testListGames() {
        facade.register("player4", "password", "p4@email.com");
        facade.login("player4", "password");

        facade.createGame("TestGame1");
        facade.createGame("TestGame2");

        var games = facade.listGames();
        assertNotNull(games, "Games list should not be null");
        assertTrue(games.size() >= 2, "Should list at least two games after creation");

        boolean foundGame1 = games.stream().anyMatch(game -> "TestGame1".equals(game.gameName()));
        boolean foundGame2 = games.stream().anyMatch(game -> "TestGame2".equals(game.gameName()));
        assertTrue(foundGame1, "TestGame1 should be listed");
        assertTrue(foundGame2, "TestGame2 should be listed");
    }

    @Test
    public void testListGamesWithoutAuth() {
        facade.logout(); // Ensure the user is logged out
        var games = facade.listGames();
        assertTrue(games == null || games.isEmpty(), "Should return an empty list or null without authentication.");
    }

    @Test
    public void testJoinGameSuccess() {
        facade.register("player5", "password", "p5@email.com");
        facade.login("player5", "password");
        int gameId = facade.createGame("JoinableGame");

        boolean joined = facade.joinGame(gameId, "WHITE");
        assertTrue(joined, "Joining game should succeed with valid ID and color");
    }

    @Test
    public void testJoinGameFail() {
        facade.register("player6", "password", "p6@email.com");
        facade.login("player6", "password");

        boolean joined = facade.joinGame(9999, "WHITE");  // Trying to join a non-existent game
        assertFalse(joined, "Joining game should fail with invalid game ID");
    }

    @Test
    public void testJoinGameFailWithInvalidID() {
        facade.register("player4", "password", "p4@email.com");
        facade.login("player4", "password");

        boolean joined = facade.joinGame(9999, "WHITE");  // Invalid game ID
        assertFalse(joined, "Joining game should fail with an invalid game ID");
    }

//    @Test
//    public void testObserveGameSuccess() {
//        facade.register("player5", "password", "p5@email.com");
//        facade.login("player5", "password");
//        int gameId = facade.createGame("ObservableGame");
//
//        boolean observing = facade.observeGame(gameId);
//        assertTrue(observing, "Observing game should succeed with a valid game ID");
//    }

    @Test
    public void testObserveGameFailWithInvalidID() {
        facade.register("player6", "password", "p6@email.com");
        facade.login("player6", "password");

        boolean observing = facade.observeGame(9999);  // Invalid game ID
        assertFalse(observing, "Observing game should fail with an invalid game ID");
    }

    @Test
    public void testObserveGameFailWithoutLogin() {
        int gameId = 1001;
        boolean observing = facade.observeGame(gameId);
        assertFalse(observing, "Observing game should fail if the user is not logged in");
    }
}
