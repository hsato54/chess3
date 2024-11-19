package ui;

import model.GameData;

import java.util.*;

public class ServerFacade {

    private HttpCommunicator http;
    private String authToken;

    public ServerFacade() throws Exception {
        this("localhost:8080");
    }

    public ServerFacade(String serverDomain) throws Exception {
        http = new HttpCommunicator(this, serverDomain);
    }

    public String getAuthToken() {
        return authToken;
    }

    protected void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean register(String username, String password, String email) {
        return http.register(username, password, email);
    }

    public boolean login(String username, String password) {
        boolean success = http.login(username, password);
        if (success) {
            this.authToken = http.getAuthToken();
        }
        return success;
    }

    public boolean logout() {
        boolean success = http.logout();
        if (success) {
            this.authToken = null;
        }
        return success;
    }

    public int createGame(String gameName) {
        return http.createGame(gameName);
    }

    public HashSet<GameData> listGames() {
        return http.listGames();
    }

    public boolean joinGame(int gameId, String playerColor) {
        return http.joinGame(gameId, playerColor);
    }

    public boolean observeGame(int gameId) {
        return http.observeGame(gameId);
    }
    public void clear() {
        http.clear();
    }

}
