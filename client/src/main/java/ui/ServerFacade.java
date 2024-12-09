package ui;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import model.GameData;
import websocket.commands.*;

import java.io.IOException;
import java.util.*;

public class ServerFacade {

    private HttpCommunicator http;
    private String authToken;
    private WebsocketCommunicator ws;
    private String server;

    public ServerFacade() throws Exception {
        this("localhost:8080");
    }

    public ServerFacade(String serverDomain) throws Exception {
        http = new HttpCommunicator(this, serverDomain);
        this.server = serverDomain;
        connectWS();
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

    public boolean joinGame(int gameId, String playerColor) throws IOException {
        ChessGame.TeamColor color;
        if (playerColor.equals("WHITE")){
            color = ChessGame.TeamColor.WHITE;
        }
        else{
            color = ChessGame.TeamColor.BLACK;
        }
        Connect connectCommand = new Connect(authToken, gameId, color);
        http.joinGame(gameId, playerColor);
        sendCommand(connectCommand);
        return true;
    }

    public void clear() {
        http.clear();
    }
    private void connectWS() {
        try {
            ws = new WebsocketCommunicator(server, this);
        } catch (Exception e) {
            System.out.println("Failed to establish WebSocket connection");
        }
    }
    public void sendCommand(UserGameCommand command) throws IOException {
        String message = new Gson().toJson(command);
        ws.sendMessage(message);
    }
    public void connect(int gameID, ChessGame.TeamColor color) throws IOException {
        Connect connectCommand = new Connect(authToken, gameID, color);
        sendCommand(connectCommand);
    }

    public void makeMove(int gameID, ChessMove move) throws IOException {
        sendCommand(new MakeMove(authToken, gameID, move));
    }

    public void leave(int gameID) throws IOException {
        sendCommand(new Leave(authToken, gameID));
    }

    public void resign(int gameID) throws IOException {
        sendCommand(new Resign(authToken, gameID));
    }

    public ChessGame getGame(int gameID) {
        GameData gameData = http.getGameByID(gameID);
        return gameData != null ? gameData.game() : null;
    }
    public void observeGame(int gameID) throws IOException {
        Connect connectCommand = new Connect(authToken, gameID, null);
        sendCommand(connectCommand);
    }

}

