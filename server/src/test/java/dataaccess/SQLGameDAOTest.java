import dataaccess.SQLUserDAO;
import dataaccess.DataAccessException;
import model.UserData;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTest {

    private SQLUserDAO userDAO;

    @BeforeAll
    static void setUpBeforeClass() {
        // Configure H2 as an in-memory database for tests
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        DatabaseManager.setDataSource(dataSource); // Assume DatabaseManager can set a custom DataSource
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new SQLUserDAO();

        // Create the users table in H2
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255),
                    PRIMARY KEY (username)
                )
            """);
        } catch (SQLException e) {
            throw new DataAccessException("Error setting up test database", e);
        }

        // Clear the table before each test
        userDAO.clear();
    }

    @AfterEach
    void tearDown() {
        userDAO.clear();
    }
}
