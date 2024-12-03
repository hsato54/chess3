package ui;

import chess.*;

import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class GameplayUI {

    private final ChessBoard chessBoard;
    private final Scanner scanner;
    private final ServerFacade server;
    private final int gameID;
    private boolean isWhiteAtBottom;

    public GameplayUI(ServerFacade server, int gameID, boolean isPlayerWhite) {
        this.chessBoard = new ChessBoard();
        this.chessBoard.resetBoard();
        this.scanner = new Scanner(System.in);
        this.server = server;
        this.gameID = gameID;
        this.isWhiteAtBottom = isPlayerWhite;
    }

    public void run() {
        System.out.println("Game started! Type 'help' to see available commands.");

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

    private void displayHelp() {
        System.out.println("Available Commands:");
        System.out.println("help - Display this menu");
        System.out.println("redraw - Redraw the chessboard");
        System.out.println("leave - Exit the game");
        System.out.println("move - Make a move (e.g., 'e2 e4')");
        System.out.println("resign - Resign the game");
        System.out.println("highlight - Highlight legal moves for a piece");
    }

    private void redrawBoard() {
        System.out.println("Redrawing the board...");
        displayBoardOrientation(isWhiteAtBottom);
    }
    private void leaveGame() {
        System.out.println("You have left the game. Returning to main menu...");
        server.leave(gameID);
    }
    private void resignGame() {
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
            String[] positions = moveInput.split(" ");
            if (positions.length != 2) {
                System.out.println("Invalid move format. Use 'move e2 e4'.");
                return;
            }

            ChessPosition from = ChessPosition.fromAlgebraic(positions[0]);
            ChessPosition to = ChessPosition.fromAlgebraic(positions[1]);
            ChessMove move = new ChessMove(from, to, );

            server.makeMove(gameID, move);
            System.out.println("Move sent to server.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid position format. Use algebraic notation (e.g., 'e2 e4').");
        }
    }
    private void highlightLegalMoves() {
        System.out.print("Enter the position of the piece to highlight (e.g., 'e2'): ");
        String positionInput = scanner.nextLine().trim();

        try {
            ChessPosition position = ChessPosition.fromAlgebraic(positionInput);
            ChessPiece piece = chessBoard.getPiece(position);

            if (piece == null) {
                System.out.println("No piece at the specified position.");
                return;
            }

            Collection<ChessMove> validMoves = server.getGame(gameID).validMoves(position);
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
                ChessPiece piece = chessBoard.getPiece(currentPos);

                if (isLightSquare) {
                    System.out.print(SET_BG_COLOR_LIGHT_GREY);
                } else {
                    System.out.print(SET_BG_COLOR_DARK_GREY);
                }

                if (highlights.contains(currentPos)) {
                    System.out.print(SET_BG_COLOR_GREEN);
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
        System.out.println("Displaying initial board setup...\n");

        displayBoardOrientation(true);

        System.out.println("\n");

        displayBoardOrientation(false);
    }

    private void displayBoardOrientation(boolean whiteAtBottom) {
        System.out.println(whiteAtBottom ? "White pieces at the bottom:" : "Black pieces at the bottom:");

        for (int row = (whiteAtBottom ? 7 : 0); (whiteAtBottom ? row >= 0 : row < 8); row += (whiteAtBottom ? -1 : 1)) {
            int displayedRowNumber = row + 1;
            System.out.print(displayedRowNumber + " ");

            boolean isLightSquare = (whiteAtBottom ? row % 2 == 1 : row % 2 == 0);

            for (int col = 0; col < 8; col++) {

                int adjustedCol = whiteAtBottom ? col : 7 - col;

                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row + 1, adjustedCol + 1));

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
        for (char file = (whiteAtBottom ? 'a' : 'h'); (whiteAtBottom ? file <= 'h' : file >= 'a'); file += (whiteAtBottom ? 1 : -1)) {
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
}
