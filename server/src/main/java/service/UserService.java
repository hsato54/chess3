package service;

import dataaccess.UnauthorizedException;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.UserData;
import model.AuthData;
import dataaccess.DataAccessException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;



public class UserService {

    private UserDAO userDAO;
    private AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }



    public AuthData createUser(UserData user) throws DataAccessException{
        if (userDAO.getUser(user.username()) != null) {
            throw new DataAccessException("User already exists.");
        }

        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        user = new UserData(user.username(), hashedPassword, user.email());
        userDAO.createUser(user);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, user.username());
        authDAO.createAuth(authData);
        return authData;
    }

    public AuthData login(UserData user) throws DataAccessException{

        UserData existingUser = userDAO.getUser(user.username());
        if (existingUser == null || !BCrypt.checkpw(user.password(), existingUser.password())) {
            throw new DataAccessException("Invalid username or password.");
        }


        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, user.username());
        authDAO.createAuth(authData);
        return authData;
    }


    public void logout(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Invalid auth token.");
        }
        authDAO.deleteAuth(authToken);
    }
    public AuthData getAuth(String authToken) throws UnauthorizedException {
        try {
            return authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
    }


}
