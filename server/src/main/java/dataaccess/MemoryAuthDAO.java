package dataaccess;

import model.AuthData;

import java.util.HashSet;
import java.util.Set;

public class MemoryAuthDAO implements AuthDAO {

    private Set<AuthData> authDataSet;

    public MemoryAuthDAO() {
        authDataSet = new HashSet<>();
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        if (!authDataSet.contains(authData)) {
            authDataSet.add(authData);
        } else {
            throw new DataAccessException("This auth token already exists.");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        for (AuthData auth : authDataSet) {
            if (auth.authToken().equals(authToken)) {
                return auth;
            }
        }
        throw new DataAccessException("Auth token not found.");
    }

    @Override
    public void deleteAuth(String authToken) {
        authDataSet.removeIf(auth -> auth.authToken().equals(authToken));
    }

    @Override
    public void clear() {
        authDataSet.clear();
    }

    @Override
    public void addAuth(AuthData authData) {
        authDataSet.add(authData);
    }
}