package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor pieceColor;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public enum PieceType {
        KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    public PieceType getPieceType() {
        return type;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        switch (type) {
            case PAWN -> pawnMove(board, myPosition, validMoves);
            case ROOK -> rookMove(board, myPosition, validMoves);
            case BISHOP -> bishopMove(board, myPosition, validMoves);
            case QUEEN -> {
                bishopMove(board, myPosition, validMoves);
                rookMove(board, myPosition, validMoves);
            }
            case KNIGHT -> knightMove(board, myPosition, validMoves);
            case KING -> kingMove(board, myPosition, validMoves);
        }
        return validMoves;
    }

    private void pawnMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves) {
        int direction = pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        addPawnAdvanceMoves(board, validMoves, row, col, direction, myPosition);
        addPawnCaptureMoves(board, validMoves, row, col, direction, myPosition);
    }

    private void addPawnAdvanceMoves(ChessBoard board, Collection<ChessMove> validMoves,
                                     int row, int col, int direction, ChessPosition myPosition) {
        ChessPosition oneStep = new ChessPosition(row + direction, col);
        if (moveAble(oneStep) && board.getPiece(oneStep) == null) {
            if (isPromotionRow(row + direction)) {
                addPromotionMoves(validMoves, myPosition, oneStep);
            } else {
                validMoves.add(new ChessMove(myPosition, oneStep, null));
            }
            ChessPosition twoStep = new ChessPosition(row + 2 * direction, col);
            if (isStartingPawnRow(row) && board.getPiece(twoStep) == null) {
                validMoves.add(new ChessMove(myPosition, twoStep, null));
            }
        }
    }

    private void addPawnCaptureMoves(ChessBoard board, Collection<ChessMove> validMoves,
                                     int row, int col, int direction, ChessPosition myPosition) {
        int[][] captureOffsets = {{direction, -1}, {direction, 1}};
        for (int[] offset : captureOffsets) {
            ChessPosition capturePos = new ChessPosition(row + offset[0], col + offset[1]);
            if (moveAble(capturePos) && isOpponentPiece(board.getPiece(capturePos))) {
                if (isPromotionRow(row + direction)) {
                    addPromotionMoves(validMoves, myPosition, capturePos);
                } else {
                    validMoves.add(new ChessMove(myPosition, capturePos, null));
                }
            }
        }
    }

    private void rookMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves) {
        linearMoves(board, myPosition, validMoves, new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}});
    }

    private void bishopMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves) {
        linearMoves(board, myPosition, validMoves, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
    }

    private void linearMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves, int[][] directions) {
        for (int[] dir : directions) {
            int row = start.getRow();
            int col = start.getColumn();
            while (true) {
                row += dir[0];
                col += dir[1];
                ChessPosition newPos = new ChessPosition(row, col);
                if (!moveAble(newPos) || !addMoveIfValid(board, start, newPos, moves)) {
                    break;
                }
            }
        }
    }

    private void knightMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves) {
        int[][] offsets = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        for (int[] offset : offsets) {
            ChessPosition newPos = new ChessPosition(myPosition.getRow() + offset[0], myPosition.getColumn() + offset[1]);
            addMoveIfValid(board, myPosition, newPos, validMoves);
        }
    }

    private void kingMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves) {
        int[][] offsets = {
                {1, 0}, {0, 1}, {-1, 0}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
        for (int[] offset : offsets) {
            ChessPosition newPos = new ChessPosition(myPosition.getRow() + offset[0], myPosition.getColumn() + offset[1]);
            addMoveIfValid(board, myPosition, newPos, validMoves);
        }
    }

    private boolean moveAble(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        return row > 0 && row <= 8 && col > 0 && col <= 8;
    }

    private boolean isOpponentPiece(ChessPiece piece) {
        return piece != null && piece.getTeamColor() != this.pieceColor;
    }

    private boolean isPromotionRow(int row) {
        return (row == 8 && pieceColor == ChessGame.TeamColor.WHITE) || (row == 1 && pieceColor == ChessGame.TeamColor.BLACK);
    }

    private boolean isStartingPawnRow(int row) {
        return (row == 2 && pieceColor == ChessGame.TeamColor.WHITE) || (row == 7 && pieceColor == ChessGame.TeamColor.BLACK);
    }

    private boolean addMoveIfValid(ChessBoard board, ChessPosition start, ChessPosition end, Collection<ChessMove> moves) {
        ChessPiece piece = board.getPiece(end);
        if (piece == null) {
            moves.add(new ChessMove(start, end, null));
            return true;
        } else if (isOpponentPiece(piece)) {
            moves.add(new ChessMove(start, end, null));
        }
        return false;
    }

    private void addPromotionMoves(Collection<ChessMove> moves, ChessPosition start, ChessPosition end) {
        moves.add(new ChessMove(start, end, PieceType.QUEEN));
        moves.add(new ChessMove(start, end, PieceType.ROOK));
        moves.add(new ChessMove(start, end, PieceType.BISHOP));
        moves.add(new ChessMove(start, end, PieceType.KNIGHT));
    }
}
