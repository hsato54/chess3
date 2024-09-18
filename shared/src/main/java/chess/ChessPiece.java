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

        //throw new RuntimeException("Not implemented");
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {

        //throw new RuntimeException("Not implemented");
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
        //throw new RuntimeException("Not implemented");
        Collection<ChessMove> validMoves = new ArrayList<>();

        if (type == PieceType.PAWN) {
            pawnMove(board, myPosition, validMoves);
        }
        return validMoves;
    }

    private void pawnMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves){

        int direction;
        if (pieceColor == ChessGame.TeamColor.WHITE){
            direction = 1;
        }
        else {
            direction = -1;
        }

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        if (moveAble(row + direction, col) && board.getPiece(new ChessPosition(row + direction, col)) == null){
            validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col), null));

            if ((row == 1 && pieceColor == ChessGame.TeamColor.WHITE) || (row == 6 && pieceColor == ChessGame.TeamColor.BLACK)){
                if (board.getPiece(new ChessPosition(row + 2 * direction, col)) == null) {
                    validMoves.add(new ChessMove(myPosition, new ChessPosition(row + 2 * direction, col), null));
                }
            }
        }
        if (moveAble(row + direction, col - 1) && theOpp(board.getPiece(new ChessPosition(row + direction, col - 1)))){
            validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col - 1), null));
        }
        if (moveAble(row + direction, col + 1) && theOpp(board.getPiece(new ChessPosition(row + direction, col + 1)))){
            validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col + 1), null));
        }



    }
    private boolean moveAble(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
    private boolean theOpp(ChessPiece piece) {
        return piece != null && piece.getTeamColor() != this.pieceColor;
    }
}
