package service;

import dataaccess.*;
import service.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    private static ClearService clearService;
    private static UserDAO userDAO;
    private static GameDAO gameDAO;
    private static AuthDAO authDAO;

    @BeforeAll
    static void init() {
        // Initialize DAOs and ClearService
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        clearService = new ClearService(userDAO, gameDAO, authDAO);
    }

    @BeforeEach
    void setup() throws DataAccessException {
        // Clear any data before each test
        userDAO.clear();
        authDAO.clear();

        // Add test data for positive test cases
        userDAO.createUser(new UserData("testUser", "password", "test@example.com"));
        authDAO.createAuth(new AuthData("authToken", "testUser"));
    }

    @Test
    @DisplayName("Clear All Data - Positive Case")
    void clearTestPositive() throws DataAccessException {
        // Pre-check: Ensure DAOs contain data
        assertNotNull(userDAO.getUser("testUser"), "UserDAO should contain user data before clear");
        assertNotNull(authDAO.getAuth("authToken"), "AuthDAO should contain auth data before clear");

        // Call clear method
        clearService.clear();

        // Post-check: Verify DAOs are empty after clear
        assertNull(userDAO.getUser("testUser"), "UserDAO should be empty after clear");
        assertNull(authDAO.getAuth("authToken"), "AuthDAO should be empty after clear");
    }

    @Test
    @DisplayName("Clear Empty DAOs - Negative Case")
    void clearTestNegative() throws DataAccessException {
        // Clear DAOs explicitly to simulate empty DAOs before clearing
        userDAO.clear();
        authDAO.clear();

        // Pre-check: Confirm DAOs are already empty
        assertNull(userDAO.getUser("testUser"), "UserDAO should be empty before clear");
        assertNull(authDAO.getAuth("authToken"), "AuthDAO should be empty before clear");

        // Call clear method on already empty DAOs
        assertDoesNotThrow(() -> clearService.clear(), "Clearing empty DAOs should not throw an exception");

        // Post-check: DAOs remain empty without errors
        assertNull(userDAO.getUser("testUser"), "UserDAO should remain empty after clear");
        assertNull(authDAO.getAuth("authToken"), "AuthDAO should remain empty after clear");
    }
}
