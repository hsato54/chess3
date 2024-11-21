package ui;

import chess.*;

import static ui.EscapeSequences.*;

public class GameplayUI {

    private final ChessBoard chessBoard;

    public GameplayUI() {
        this.chessBoard = new ChessBoard();
        this.chessBoard.resetBoard();
    }

    public void displayBoard() {
        System.out.println("Displaying initial board setup...\n");

        displayBoardOrientation(true);

        System.out.println("\n");

        displayBoardOrientation(false);
    }

    private void displayBoardOrientation(boolean whiteAtBottom) {
        System.out.println(whiteAtBottom ? "White pieces at the bottom:" : "Black pieces at the bottom:");

        boolean isLightSquare = whiteAtBottom;
        for (int row = (whiteAtBottom ? 7 : 0); (whiteAtBottom ? row >= 0 : row < 8); row += (whiteAtBottom ? -1 : 1)) {
            System.out.print((whiteAtBottom ? row + 1 : 8 - row) + " ");

            for (int col = 0; col < 8; col++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row + 1, col + 1));

                if (isLightSquare) {
                    System.out.print(SET_BG_COLOR_LIGHT_GREY);
                } else {
                    System.out.print(SET_BG_COLOR_DARK_GREY);
                }

                if (piece != null) {
                    printPiece(piece);
                } else {
                    System.out.print(EMPTY);
                }

                System.out.print(RESET_BG_COLOR);
                isLightSquare = !isLightSquare;
            }

            System.out.println(" " + (whiteAtBottom ? row + 1 : 8 - row));
            isLightSquare = !isLightSquare;
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
                case KING -> pieceSymbol = WHITE_KING;
                case QUEEN -> pieceSymbol = WHITE_QUEEN;
                case BISHOP -> pieceSymbol = WHITE_BISHOP;
                case KNIGHT -> pieceSymbol = WHITE_KNIGHT;
                case ROOK -> pieceSymbol = WHITE_ROOK;
                case PAWN -> pieceSymbol = WHITE_PAWN;
                default -> pieceSymbol = " ";
            }
        } else {
            switch (piece.getPieceType()) {
                case KING -> pieceSymbol = BLACK_KING;
                case QUEEN -> pieceSymbol = BLACK_QUEEN;
                case BISHOP -> pieceSymbol = BLACK_BISHOP;
                case KNIGHT -> pieceSymbol = BLACK_KNIGHT;
                case ROOK -> pieceSymbol = BLACK_ROOK;
                case PAWN -> pieceSymbol = BLACK_PAWN;
                default -> pieceSymbol = " ";
            }
        }
        System.out.print(pieceSymbol);
    }
}
