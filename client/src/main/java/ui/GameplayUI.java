package ui;

import chess.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class GameplayUI {

    private static GameplayUI currentInstance;
    private ChessGame chessGame;
    private final Scanner scanner;
    private final ServerFacade server;
    private final int gameID;
    private boolean isWhiteAtBottom;

    private GameplayUI(ServerFacade server, int gameID, boolean isPlayerWhite) {
        this.scanner = new Scanner(System.in);
        this.server = server;
        this.gameID = gameID;
        this.isWhiteAtBottom = isPlayerWhite;
        this.chessGame = null;
    }

    public static GameplayUI getInstance(ServerFacade server, int gameID, boolean isPlayerWhite) {
        if (currentInstance == null || currentInstance.gameID != gameID) {
            currentInstance = new GameplayUI(server, gameID, isPlayerWhite);
        }
        return currentInstance;
    }

    public static GameplayUI getCurrentInstance() {
        return currentInstance;
    }

    public void run() throws IOException {

        System.out.println("Game started! Type 'help' to see available commands.");

        if (chessGame != null) {
            redrawBoard(); // Ensure the correct orientation is displayed
        }

        while (true) {
            System.out.print("[IN-GAME] >>> ");
            String command = scanner.nextLine().trim().toLowerCase();

            if (command.equals("help")) {
                displayHelp();
            } else if (command.equals("redraw")) {
                redrawBoard();
            } else if (command.equals("leave")) {
                leaveGame();
                return;
            } else if (command.equals("resign")) {
                resignGame();
                return;
            } else if (command.equals("highlight")) {
                highlightLegalMoves();
            } else if (command.equals("move")) {
                makeMove();
            } else {
                System.out.println("Unknown command. Type 'help' for a list of commands.");
            }
        }
    }

    public void runobserve() throws IOException {
        System.out.println("Observing the game! Type 'help' to see available commands.");

        while (true) {
            System.out.print("[OBSERVING] >>> ");
            String command = scanner.nextLine().trim().toLowerCase();

            if (command.equals("help")) {
                displayObserverHelp();
            } else if (command.equals("redraw")) {
                redrawBoard();
            } else if (command.equals("highlight")) {
                highlightLegalMoves();
            } else if (command.equals("leave")) {
                leaveGame();
                break;
            } else {
                System.out.println("Unknown command. Type 'help' for available commands.");
            }
        }
    }

    private void displayHelp() {
        System.out.println("Available Commands:");
        System.out.println("help - Display this menu");
        System.out.println("redraw - Redraw the chessboard");
        System.out.println("leave - Exit the game");
        System.out.println("move - Make a move (e.g., 'e2 e4')");
        System.out.println("resign - Resign the game");
        System.out.println("highlight - Highlight legal moves for a piece");
    }
    private void displayObserverHelp(){
        System.out.println("Available Commands for Observers:");
        System.out.println("help - Display this menu");
        System.out.println("redraw - Redraw the chessboard");
        System.out.println("leave - Stop observing the game");
        System.out.println("highlight - Highlight legal moves for a piece");
    }
    private void redrawBoard() {
        System.out.println("Redrawing the board...");
        displayBoardOrientation();
    }
    private void leaveGame() throws IOException {
        System.out.println("You have left the game. Returning to main menu...");
        server.leave(gameID);
    }
    private void resignGame() throws IOException {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (confirmation.equals("yes")) {
            System.out.println("You have resigned from the game.");
            server.resign(gameID);
        } else {
            System.out.println("Resignation cancelled.");
        }
    }
    private void makeMove() {
        System.out.print("Enter your move (e.g., 'e2 e4'): ");
        String moveInput = scanner.nextLine().trim();

        try {
            String[] parts = moveInput.split(" ");
            if (parts.length < 2 || parts.length > 3) {
                System.out.println("Invalid move format. Use 'move e2 e4'.");
                return;
            }

            ChessPosition from = ChessPosition.fromAlgebraic(parts[0]);
            ChessPosition to = ChessPosition.fromAlgebraic(parts[1]);

            ChessPiece.PieceType promo = null;

            if (parts.length == 3) {
                promo = switch (parts[2].toLowerCase()) {
                    case "q" -> ChessPiece.PieceType.QUEEN;
                    case "r" -> ChessPiece.PieceType.ROOK;
                    case "b" -> ChessPiece.PieceType.BISHOP;
                    case "n" -> ChessPiece.PieceType.KNIGHT;
                    default -> throw new IllegalArgumentException("Invalid promotion piece. Use 'q', 'r', 'b', or 'n'.");
                };
            }

            ChessMove move = new ChessMove(from, to, promo);

            server.makeMove(gameID, move);
            System.out.println("Move sent to server.");


        } catch (IllegalArgumentException e) {
            System.out.println("Invalid position format. Use algebraic notation (e.g., 'e2 e4').");
        }catch(IOException io){
            System.out.println("ioexception");
        }catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }

    }
    private void highlightLegalMoves() {
        System.out.print("Enter the position of the piece to highlight (e.g., 'e2'): ");
        String positionInput = scanner.nextLine().trim();

        try {
            ChessPosition position = ChessPosition.fromAlgebraic(positionInput);
            ChessPiece piece = chessGame.getBoard().getPiece(position);
            Collection<ChessMove> validMoves = chessGame.validMoves(position);

            if (piece == null) {
                System.out.println("No piece at the specified position.");
                return;
            }
            if (validMoves.isEmpty()) {
                System.out.println("No legal moves available for this piece.");
                return;
            }

            System.out.println("Highlighting legal moves...");
            redrawBoardWithHighlights(validMoves);

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid position format. Use algebraic notation (e.g., 'e2').");
        }
    }
    private void redrawBoardWithHighlights(Collection<ChessMove> highlights) {
        for (int row = (isWhiteAtBottom ? 7 : 0); (isWhiteAtBottom ? row >= 0 : row < 8); row += (isWhiteAtBottom ? -1 : 1)) {
            int displayedRowNumber = row + 1;
            System.out.print(displayedRowNumber + " ");

            boolean isLightSquare = (isWhiteAtBottom ? row % 2 == 1 : row % 2 == 0);

            for (int col = 0; col < 8; col++) {

                int adjustedCol = isWhiteAtBottom ? col : 7 - col;
                ChessPosition currentPos = new ChessPosition(row + 1, adjustedCol + 1);
                ChessPiece piece = chessGame.getBoard().getPiece(currentPos);

                if (isLightSquare) {
                    System.out.print(SET_BG_COLOR_LIGHT_GREY);
                } else {
                    System.out.print(SET_BG_COLOR_DARK_GREY);
                }

                for (ChessMove startpos : highlights){
                    if (startpos.getEndPosition().equals(currentPos) || startpos.getStartPosition().equals(currentPos)) {
                        System.out.print(SET_BG_COLOR_GREEN);
                    }
                }

                if (piece != null) {
                    printPiece(piece);
                } else {
                    System.out.print("   ");
                }

                System.out.print(RESET_BG_COLOR);
                isLightSquare = !isLightSquare;
            }

            System.out.println(" " + displayedRowNumber);
        }

        System.out.print("  ");
        for (char file = (isWhiteAtBottom ? 'a' : 'h'); (isWhiteAtBottom ? file <= 'h' : file >= 'a'); file += (isWhiteAtBottom ? 1 : -1)) {
            System.out.print(" " + file + " ");
        }
        System.out.println();
    }





    public void displayBoard() {
        System.out.println("Displaying current board setup...\n");
        displayBoardOrientation();
    }

    private void displayBoardOrientation() {
        System.out.println(isWhiteAtBottom ? "White pieces at the bottom:" : "Black pieces at the bottom:");

        for (int row = (isWhiteAtBottom ? 7 : 0); (isWhiteAtBottom ? row >= 0 : row < 8); row += (isWhiteAtBottom ? -1 : 1)) {
            int displayedRowNumber = row + 1;
            System.out.print(displayedRowNumber + " ");

            boolean isLightSquare = (isWhiteAtBottom ? row % 2 == 1 : row % 2 == 0);

            for (int col = 0; col < 8; col++) {

                int adjustedCol = isWhiteAtBottom ? col : 7 - col;

                ChessPiece piece = chessGame.getBoard().getPiece(new ChessPosition(row + 1, adjustedCol + 1));

                if (isLightSquare) {
                    System.out.print(SET_BG_COLOR_LIGHT_GREY);
                } else {
                    System.out.print(SET_BG_COLOR_DARK_GREY);
                }

                if (piece != null) {
                    printPiece(piece);
                } else {
                    System.out.print("   ");
                }

                System.out.print(RESET_BG_COLOR);
                isLightSquare = !isLightSquare;
            }

            System.out.println(" " + displayedRowNumber);
        }

        System.out.print("  ");
        for (char file = (isWhiteAtBottom ? 'a' : 'h'); (isWhiteAtBottom ? file <= 'h' : file >= 'a'); file += (isWhiteAtBottom ? 1 : -1)) {
            System.out.print(" " + file + " ");
        }
        System.out.println();
    }

    private void printPiece(ChessPiece piece) {

        String pieceSymbol;

        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            switch (piece.getPieceType()) {
                case KING -> pieceSymbol = " K ";
                case QUEEN -> pieceSymbol = " Q ";
                case BISHOP -> pieceSymbol = " B ";
                case KNIGHT -> pieceSymbol = " N ";
                case ROOK -> pieceSymbol = " R ";
                case PAWN -> pieceSymbol = " P ";
                default -> pieceSymbol = " ";
            }
        } else {
            switch (piece.getPieceType()) {
                case KING -> pieceSymbol = " k ";
                case QUEEN -> pieceSymbol = " q ";
                case BISHOP -> pieceSymbol = " b ";
                case KNIGHT -> pieceSymbol = " n ";
                case ROOK -> pieceSymbol = " r ";
                case PAWN -> pieceSymbol = " p ";
                default -> pieceSymbol = " ";
            }
        }
        System.out.print(pieceSymbol);
    }
    public void updateGame(ChessGame game) {
        if (game == null) {
            System.out.println("Error: Received null game object.");
            return;
        }
        this.chessGame = game;
    }

}