package service;

import dataaccess.*;
import service.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private static UserService userService;
    private static UserDAO userDAO;
    private static AuthDAO authDAO;

    private static UserData defaultUser;

    @BeforeAll
    static void init() {
        // Initialize with in-memory DAOs for testing
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @BeforeEach
    void setup() {
        // Clear data before each test
        userDAO.clear();
        authDAO.clear();

        // Define a default user for testing
        defaultUser = new UserData("Username", "password", "email@example.com");
    }

    @Test
    @DisplayName("Create Valid User")
    void createUserTestPositive() throws DataAccessException {
        AuthData resultAuth = userService.createUser(defaultUser);
        assertNotNull(resultAuth, "AuthData should not be null after creating a valid user");
        assertEquals(authDAO.getAuth(resultAuth.authToken()), resultAuth, "AuthData should be retrievable after creation");
    }

    @Test
    @DisplayName("Create User with Duplicate Username")
    void createUserTestNegative() throws DataAccessException {
        userService.createUser(defaultUser); // First creation should succeed
        assertThrows(DataAccessException.class, () -> userService.createUser(defaultUser),
                "Creating a user with an existing username should throw DataAccessException");
    }

    @Test
    @DisplayName("Login with Valid Credentials")
    void loginUserTestPositive() throws DataAccessException {
        userService.createUser(defaultUser);
        AuthData authData = userService.login(defaultUser);
        assertNotNull(authData, "AuthData should not be null for valid login");
        assertEquals(authDAO.getAuth(authData.authToken()), authData, "AuthData should be retrievable after login");
    }

    @Test
    @DisplayName("Login with Invalid Credentials")
    void loginUserTestNegative() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> userService.login(defaultUser),
                "Logging in with non-existent user credentials should throw DataAccessException");

        userService.createUser(defaultUser);
        UserData badPasswordUser = new UserData(defaultUser.username(), "wrongPassword", defaultUser.email());
        assertThrows(DataAccessException.class, () -> userService.login(badPasswordUser),
                "Logging in with incorrect password should throw DataAccessException");
    }

    @Test
    @DisplayName("Logout with Valid Token")
    void logoutUserTestPositive() throws DataAccessException {
        AuthData auth = userService.createUser(defaultUser);
        userService.logout(auth.authToken());
        assertNull(authDAO.getAuth(auth.authToken()), "Auth token should be deleted after logout");
    }

    @Test
    @DisplayName("Logout with Invalid Token")
    void logoutUserTestNegative() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> userService.logout("invalidToken"),
                "Logging out with an invalid token should throw DataAccessException");
    }
}
