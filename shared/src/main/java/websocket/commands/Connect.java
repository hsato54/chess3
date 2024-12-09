package websocket.commands;

import chess.ChessGame;

public class Connect extends UserGameCommand {

    private final String role;
    ChessGame.TeamColor playerColor;

    public Connect(String authToken, int gameID, ChessGame.TeamColor playerColor) {
        super(CommandType.CONNECT, authToken, gameID);
        this.role = (playerColor == null) ? "OBSERVER" : "PLAYER";
        this.playerColor = playerColor;
    }

    public ChessGame.TeamColor getColor() {
        return playerColor;
    }
    public String getRole() {
        return role;
    }
}