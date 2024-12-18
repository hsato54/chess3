package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * the signature of the existing methods.
 */
public class ChessPiece {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        if (type == PieceType.PAWN) {
            pawnMove(board, myPosition, validMoves);
        }
        if (type == PieceType.ROOK) {
            rookMove(board, myPosition, validMoves);
        }
        if (type == PieceType.BISHOP) {
            bishopMove(board, myPosition, validMoves);
        }
        if (type == PieceType.QUEEN) {
            bishopMove(board, myPosition, validMoves);
            rookMove(board, myPosition, validMoves);
        }
        if (type == PieceType.KNIGHT) {
            knightMove(board, myPosition, validMoves);
        }
        if (type == PieceType.KING) {
            kingMove(board, myPosition, validMoves);
        }

        return validMoves;
    }

    private void pawnMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves) {
        int direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        if (moveAble(row + direction, col) && board.getPiece(new ChessPosition(row + direction, col)) == null) {
            addPawnPromotionMoves(validMoves, myPosition, row + direction, col);
            if ((row == 2 && pieceColor == ChessGame.TeamColor.WHITE) || (row == 7 && pieceColor == ChessGame.TeamColor.BLACK)) {
                if (board.getPiece(new ChessPosition(row + 2 * direction, col)) == null) {
                    validMoves.add(new ChessMove(myPosition, new ChessPosition(row + 2 * direction, col), null));
                }
            }
        }
        checkPawnCapture(board, myPosition, validMoves, direction, row, col, -1);
        checkPawnCapture(board, myPosition, validMoves, direction, row, col, 1);
    }

    private void addPawnPromotionMoves(Collection<ChessMove> validMoves, ChessPosition startPos, int endRow, int col) {
        if ((endRow == 8 && pieceColor == ChessGame.TeamColor.WHITE) || (endRow == 1 && pieceColor == ChessGame.TeamColor.BLACK)) {
            validMoves.add(new ChessMove(startPos, new ChessPosition(endRow, col), ChessPiece.PieceType.QUEEN));
            validMoves.add(new ChessMove(startPos, new ChessPosition(endRow, col), ChessPiece.PieceType.ROOK));
            validMoves.add(new ChessMove(startPos, new ChessPosition(endRow, col), ChessPiece.PieceType.BISHOP));
            validMoves.add(new ChessMove(startPos, new ChessPosition(endRow, col), ChessPiece.PieceType.KNIGHT));
        } else {
            validMoves.add(new ChessMove(startPos, new ChessPosition(endRow, col), null));
        }
    }

    private void checkPawnCapture(ChessBoard board, ChessPosition startPos, Collection<ChessMove> validMoves,
                                  int direction, int row, int col, int colOffset) {
        if (moveAble(row + direction, col + colOffset) && theOpp(board.getPiece(new ChessPosition(row + direction, col + colOffset)))) {
            addPawnPromotionMoves(validMoves, startPos, row + direction, col + colOffset);
        }
    }

    private void rookMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves) {
        moveLinear(board, myPosition, validMoves, 1, 0);
        moveLinear(board, myPosition, validMoves, -1, 0);
        moveLinear(board, myPosition, validMoves, 0, 1);
        moveLinear(board, myPosition, validMoves, 0, -1);
    }

    private void bishopMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves) {
        moveLinear(board, myPosition, validMoves, 1, 1);
        moveLinear(board, myPosition, validMoves, 1, -1);
        moveLinear(board, myPosition, validMoves, -1, 1);
        moveLinear(board, myPosition, validMoves, -1, -1);
    }

    private void potentialMoves(ChessBoard board, ChessPosition startPos, Collection<ChessMove> validMoves, int[][] possibleMoves) {
        int row = startPos.getRow();
        int col = startPos.getColumn();

        for (int[] move : possibleMoves) {
            int endRow = row + move[0];
            int endCol = col + move[1];
            if (moveAble(endRow, endCol)) {
                ChessPosition newPos = new ChessPosition(endRow, endCol);
                ChessPiece pieceAtNewPos = board.getPiece(newPos);
                if (pieceAtNewPos == null || theOpp(pieceAtNewPos)) {
                    validMoves.add(new ChessMove(startPos, newPos, null));
                }
            }
        }
    }

    private void knightMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves) {
        int[][] possibleMoves = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        potentialMoves(board, myPosition, validMoves, possibleMoves);
    }

    private void kingMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves) {
        int[][] possibleMoves = {
                {1, -1}, {1, 0}, {1, 1},
                {0, -1}, {0, 1},
                {-1, -1}, {-1, 0}, {-1, 1}
        };
        potentialMoves(board, myPosition, validMoves, possibleMoves);
    }

    private void moveLinear(ChessBoard board, ChessPosition startPos, Collection<ChessMove> moves, int rowStep, int colStep) {
        int row = startPos.getRow();
        int col = startPos.getColumn();

        for (int i = 1; i <= 8; i++) {
            int endRow = row + i * rowStep;
            int endCol = col + i * colStep;
            if (!qbrMoves(board, startPos, endRow, endCol, moves)) {
                break;
            }
        }
    }

    private boolean moveAble(int row, int col) {
        return row > 0 && row <= 8 && col > 0 && col <= 8;
    }

    private boolean theOpp(ChessPiece piece) {
        return piece != null && piece.getTeamColor() != this.pieceColor;
    }

    private boolean friendlyBlock(ChessPiece piece) {
        return piece != null && piece.getTeamColor() == this.pieceColor;
    }

    private boolean qbrMoves(ChessBoard board, ChessPosition startPos, int endRow, int endCol, Collection<ChessMove> moves) {
        if (!moveAble(endRow, endCol)) {
            return false;
        }

        ChessPosition endPos = new ChessPosition(endRow, endCol);
        ChessPiece endPiece = board.getPiece(endPos);

        if (endPiece == null) {
            moves.add(new ChessMove(startPos, endPos, null));
            return true;
        } else if (friendlyBlock(endPiece)) {
            return false;
        } else if (theOpp(endPiece)) {
            moves.add(new ChessMove(startPos, endPos, null));
            return false;
        }
        return true;
    }
}
