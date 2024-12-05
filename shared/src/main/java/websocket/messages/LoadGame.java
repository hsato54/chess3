package websocket.messages;

import chess.ChessGame;

public class LoadGame extends ServerMessage {

    private final int gameID;
    private final ChessGame game;

    public LoadGame(int gameID, ChessGame game) {
        super(ServerMessageType.LOAD_GAME);
        this.gameID = gameID;
        this.game = game;
    }

    public int getGameID() {
        return gameID;
    }

    public ChessGame getGame() {
        return game;
    }
}