package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class MakeMove extends UserGameCommand {

    ChessMove move;

    public MakeMove(String authToken, int gameID, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        if (move == null) {
            throw new IllegalArgumentException("Move cannot be null.");
        }
        this.move = move;
    }
    public ChessMove getMove() {
        return move;
    }
}
