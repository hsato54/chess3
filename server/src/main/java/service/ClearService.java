package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class ClearService {
    private static UserDAO userDAO;
    private static GameDAO gameDAO;
    private static AuthDAO authDAO;

    public ClearService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        ClearService.userDAO = userDAO;
        ClearService.gameDAO = gameDAO;
        ClearService.authDAO = authDAO;
    }

    public static void clear() {
        userDAO.clear();
        gameDAO.clear();
        authDAO.clear();
    }
}
