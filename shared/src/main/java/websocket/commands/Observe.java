package websocket.commands;

import chess.ChessGame;

public class Observe extends UserGameCommand {

    public Observe(String authToken, int gameID) {
        super(CommandType.OBSERVE, authToken, gameID);
    }
}