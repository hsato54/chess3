import chess.*;
import ui.PreloginUI;
import ui.ServerFacade;
import ui.WebsocketCommunicator;

public class Main {
    public static void main(String[] args) throws Exception {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        String serverDomain = "localhost:8080";
        ServerFacade server = new ServerFacade(serverDomain);
        WebsocketCommunicator websocket = new WebsocketCommunicator(serverDomain, server);
        PreloginUI loginui = new PreloginUI(server);
        loginui.run();
    }
}