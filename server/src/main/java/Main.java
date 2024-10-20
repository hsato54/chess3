import chess.*;
import server.Server;

//public class Main {
//    public static void main(String[] args) {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Server: " + piece);
//    }
//}
public class Main {
    public static void main(String[] args) {
        Server server = new Server(); // create the Server object
        server.run(8080); // call the run method with the port (8080)
    }
}