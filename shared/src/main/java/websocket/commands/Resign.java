package websocket.commands;

public class Resign extends UserGameCommand {

    public Resign(String authToken, int gameID) {
        super(CommandType.RESIGN, authToken, gameID);
    }
}