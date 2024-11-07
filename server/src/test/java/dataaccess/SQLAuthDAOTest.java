package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTest {

    private static SQLAuthDAO authDAO;

    @BeforeAll
    static void init() {
        // Set up database connection for testing
        DatabaseManager.setTestConnection("jdbc:mysql://localhost/test_db", "test_user", "test_password");
        authDAO = new SQLAuthDAO();
    }

    @BeforeEach
    void setup() throws DataAccessException {
        // Clear data before each test
        authDAO.clear();

        // Add initial test data
        authDAO.createAuth(new AuthData("authToken1", "user1"));
    }

    @Test
    @DisplayName("Create Auth Token - Positive Case")
    void createAuthTestPositive() throws DataAccessException {
        AuthData newAuth = new AuthData("authToken2", "user2");

        authDAO.createAuth(newAuth);

        AuthData retrievedAuth = authDAO.getAuth("authToken2");
        assertNotNull(retrievedAuth, "Auth token should be created successfully");
        assertEquals("authToken2", retrievedAuth.authToken(), "Auth tokens should match");
        assertEquals("user2", retrievedAuth.username(), "Usernames should match");
    }

    @Test
    @DisplayName("Create Duplicate Auth Token - Negative Case")
    void createAuthTestNegative() {
        AuthData duplicateAuth = new AuthData("authToken1", "user1");

        assertThrows(DataAccessException.class, () -> authDAO.createAuth(duplicateAuth),
                "Creating a duplicate auth token should throw an exception");
    }

    @Test
    @DisplayName("Get Existing Auth Token - Positive Case")
    void getAuthTestPositive() throws DataAccessException {
        AuthData retrievedAuth = authDAO.getAuth("authToken1");

        assertNotNull(retrievedAuth, "Auth token should exist in the database");
        assertEquals("authToken1", retrievedAuth.authToken(), "Auth tokens should match");
        assertEquals("user1", retrievedAuth.username(), "Usernames should match");
    }

    @Test
    @DisplayName("Get Non-Existent Auth Token - Negative Case")
    void getAuthTestNegative() {
        assertThrows(DataAccessException.class, () -> authDAO.getAuth("nonExistentToken"),
                "Getting a non-existent auth token should throw an exception");
    }

    @Test
    @DisplayName("Delete Existing Auth Token - Positive Case")
    void deleteAuthTestPositive() throws DataAccessException {
        // Verify token exists before deletion
        assertNotNull(authDAO.getAuth("authToken1"), "Auth token should exist before deletion");

        // Delete the auth token
        authDAO.deleteAuth("authToken1");

        // Verify token no longer exists
        assertThrows(DataAccessException.class, () -> authDAO.getAuth("authToken1"),
                "Auth token should be deleted and not found afterward");
    }

    @Test
    @DisplayName("Delete Non-Existent Auth Token - Negative Case")
    void deleteAuthTestNegative() {
        // Deleting a non-existent token should not throw an exception
        assertDoesNotThrow(() -> authDAO.deleteAuth("nonExistentToken"),
                "Deleting a non-existent auth token should not throw an exception");
    }

    @Test
    @DisplayName("Clear Auth Data - Positive Case")
    void clearTestPositive() throws DataAccessException {
        assertNotNull(authDAO.getAuth("authToken1"), "AuthDAO should contain auth data before clear");

        authDAO.clear();

        assertThrows(DataAccessException.class, () -> authDAO.getAuth("authToken1"),
                "AuthDAO should be empty after clear, attempting to get a token should throw an exception");
    }

    @AfterEach
    void tearDown() throws DataAccessException {
        authDAO.clear();
    }
}
