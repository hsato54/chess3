package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTest {

    private static SQLUserDAO userDAO;

    @BeforeAll
    static void init() {
        DatabaseManager.setTestConnection("jdbc:mysql://localhost/test_db", "test_user", "test_password");
        userDAO = new SQLUserDAO();
    }

    @BeforeEach
    void setup() throws DataAccessException {
        userDAO.clear();
        userDAO.createUser(new UserData("testUser", "password", "test@example.com"));
    }

    @Test
    @DisplayName("Create User - Positive Case")
    void createUserTestPositive() throws DataAccessException {
        UserData newUser = new UserData("newUser", "newPassword", "new@example.com");
        userDAO.createUser(newUser);

        UserData retrievedUser = userDAO.getUser("newUser");
        assertNotNull(retrievedUser, "User should be created successfully");
        assertEquals("newUser", retrievedUser.username(), "Usernames should match");
        assertEquals("newPassword", retrievedUser.password(), "Passwords should match");
        assertEquals("new@example.com", retrievedUser.email(), "Emails should match");
    }

    @Test
    @DisplayName("Create User with Duplicate Username - Negative Case")
    void createUserTestNegative() {
        UserData duplicateUser = new UserData("testUser", "password", "duplicate@example.com");

        assertThrows(DataAccessException.class, () -> userDAO.createUser(duplicateUser),
                "Creating a user with duplicate username should throw an exception");
    }

    @Test
    @DisplayName("Get Existing User - Positive Case")
    void getUserTestPositive() throws DataAccessException {
        UserData retrievedUser = userDAO.getUser("testUser");

        assertNotNull(retrievedUser, "User should exist in the database");
        assertEquals("testUser", retrievedUser.username(), "Usernames should match");
        assertEquals("password", retrievedUser.password(), "Passwords should match");
        assertEquals("test@example.com", retrievedUser.email(), "Emails should match");
    }

    @Test
    @DisplayName("Get Non-Existent User - Negative Case")
    void getUserTestNegative() throws DataAccessException {
        UserData retrievedUser = userDAO.getUser("nonExistentUser");
        assertNull(retrievedUser, "Non-existent user should return null");
    }

    @Test
    @DisplayName("Clear User Data - Positive Case")
    void clearTestPositive() throws DataAccessException {
        assertNotNull(userDAO.getUser("testUser"), "UserDAO should contain user data before clear");

        userDAO.clear();

        assertNull(userDAO.getUser("testUser"), "UserDAO should be empty after clear");
    }

    @AfterEach
    void tearDown() throws DataAccessException {
        userDAO.clear();
    }
}
