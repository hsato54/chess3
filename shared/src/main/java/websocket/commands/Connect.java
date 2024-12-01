package websocket.commands;

import chess.ChessGame;

public class Connect extends UserGameCommand {

    int gameID;
    ChessGame.TeamColor playerColor;

    public Connect(String authToken, int gameID, ChessGame.TeamColor playerColor) {
        super(authToken);
        this.commandType = CommandType.JOIN_PLAYER;
        this.gameID = gameID;
        this.playerColor = playerColor;
    }

    public int getGameID() {
        return gameID;
    }

    public ChessGame.TeamColor getColor() {
        return playerColor;
    }
}