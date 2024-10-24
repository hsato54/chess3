package service;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.UserData;
import model.AuthData;
import dataaccess.DataAccessException;

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
        userDAO.createUser(user);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, user.username());
        authDAO.createAuth(authData);
        return authData;
    }

    public AuthData login(UserData user) throws DataAccessException{
        UserData existingUser = userDAO.getUser(user.username());

        if (existingUser == null || !existingUser.password().equals(user.password())) {
            throw new DataAccessException("Invalid username or password.");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, user.username());
        authDAO.createAuth(authData);
        return authData;
    }

    public void logout(String authToken) throws DataAccessException {
        // Delete the authToken from the database
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Invalid auth token.");
        }
        authDAO.deleteAuth(authToken);
    }

    public void clear(){
        userDAO.clear();
        authDAO.clear();
    }


}
