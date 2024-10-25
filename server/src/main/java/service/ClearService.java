package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;

public class ClearService {
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public ClearService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}
