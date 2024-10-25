package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;

public class ClearService {
    private static UserDAO userDAO;
    private static AuthDAO authDAO;

    public ClearService(UserDAO userDAO, AuthDAO authDAO) {
        ClearService.userDAO = userDAO;
        ClearService.authDAO = authDAO;
    }

    public static void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}
