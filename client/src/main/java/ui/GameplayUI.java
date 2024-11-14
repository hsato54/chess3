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
                    System.out.print("   ");
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

    private String getPieceSymbol(int row, int column) {
        StringBuilder output = new StringBuilder();
        ChessGame game = null;
        ChessPiece piece = game.getBoard().getPiece(new ChessPosition(row, column));

        if (piece != null) {
            output.append(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK);
            switch (piece.getPieceType()) {
                case KING -> output.append(" K ");
                case QUEEN -> output.append(" Q ");
                case BISHOP -> output.append(" B ");
                case KNIGHT -> output.append(" N ");
                case ROOK -> output.append(" R ");
                case PAWN -> output.append(" P ");
            }
        } else {
            output.append("   ");
        }

        return output.toString();
    }
}
