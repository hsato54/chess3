package websocket.commands;

import chess.ChessGame;

public class Observe extends UserGameCommand {

    int gameID;

    public Observe(String authToken, int gameID) {
        super(authToken);
        this.commandType = CommandType.JOIN_OBSERVER;
        this.gameID = gameID;
    }

    public int getGameID() {
        return gameID;
    }
}