package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;


/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
        if (type == PieceType.ROOK){
            rookMove(board, myPosition, validMoves);
        }
        if (type == PieceType.BISHOP) {
            bishopMove(board, myPosition, validMoves);
        }
        if (type == PieceType.QUEEN){
            //queenMove(board, myPosition, validMoves);
            bishopMove(board, myPosition, validMoves);
            rookMove(board, myPosition, validMoves);
        }
        if (type == PieceType.KNIGHT){
            knightMove(board, myPosition, validMoves);
        }
        if (type == PieceType.KING) {
            kingMove(board, myPosition, validMoves);
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

            if ((row + direction == 8 && pieceColor == ChessGame.TeamColor.WHITE) || (row + direction == 1 && pieceColor == ChessGame.TeamColor.BLACK)) {
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col), ChessPiece.PieceType.QUEEN));
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col), ChessPiece.PieceType.ROOK));
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col), ChessPiece.PieceType.BISHOP));
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col), ChessPiece.PieceType.KNIGHT));
            }
            else{
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col), null));
            }

            if ((row == 2 && pieceColor == ChessGame.TeamColor.WHITE) || (row == 7 && pieceColor == ChessGame.TeamColor.BLACK)){
                if (board.getPiece(new ChessPosition(row + 2 * direction, col)) == null) {
                    validMoves.add(new ChessMove(myPosition, new ChessPosition(row + 2 * direction, col), null));
                }
            }
        }
        if (moveAble(row + direction, col - 1) && theOpp(board.getPiece(new ChessPosition(row + direction, col - 1)))){
            if ((row + direction == 8 && pieceColor == ChessGame.TeamColor.WHITE) || (row + direction == 1 && pieceColor == ChessGame.TeamColor.BLACK)) {
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col - 1), ChessPiece.PieceType.QUEEN));
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col - 1), ChessPiece.PieceType.ROOK));
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col - 1), ChessPiece.PieceType.BISHOP));
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col - 1), ChessPiece.PieceType.KNIGHT));
            }
            else{
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col - 1), null));
            }
        }
        if (moveAble(row + direction, col + 1) && theOpp(board.getPiece(new ChessPosition(row + direction, col + 1)))){
            if ((row + direction == 8 && pieceColor == ChessGame.TeamColor.WHITE) || (row + direction == 1 && pieceColor == ChessGame.TeamColor.BLACK)) {
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col + 1), ChessPiece.PieceType.QUEEN));
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col + 1), ChessPiece.PieceType.ROOK));
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col + 1), ChessPiece.PieceType.BISHOP));
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col + 1), ChessPiece.PieceType.KNIGHT));
            }
            else{
                validMoves.add(new ChessMove(myPosition, new ChessPosition(row + direction, col + 1), null));
            }
        }
    }

    private void rookMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves){

        int row = myPosition.getRow();
        int col = myPosition.getColumn();


        for (int c = col - 1; c > 0; c--) {
            if (!QBRmoves(board, myPosition, row, c, validMoves)){
                break;
            }
        }
        for (int c = col + 1; c <= 8; c++) {
            if (!QBRmoves(board, myPosition, row, c, validMoves)){
                break;
            }
        }
        for (int r = row - 1; r > 0; r--) {
            if (!QBRmoves(board, myPosition, r, col, validMoves)) {
                break;
            }
        }
        for (int r = row + 1; r <= 8; r++) {
            if (!QBRmoves(board, myPosition, r, col, validMoves)) {
                break;
            }
        }

    }

    private void bishopMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves){

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        for (int c = col - 1, r = row - 1; c > 0 && r > 0; c--, r--) {
            if (!QBRmoves(board, myPosition, r, c, validMoves)){
                break;
            }
        }
        for (int c = col + 1, r = row - 1; c <= 8 && r > 0; c++, r--) {
            if (!QBRmoves(board, myPosition, r, c, validMoves)){
                break;
            }
        }
        for (int c = col - 1, r = row + 1; c > 0 && r <= 8; c--, r++) {
            if (!QBRmoves(board, myPosition, r, c, validMoves)) {
                break;
            }
        }
        for (int c = col + 1, r = row + 1; c <= 8 && r <= 8; c++, r++) {
            if (!QBRmoves(board, myPosition, r, c, validMoves)) {
                break;
            }
        }


    }
//    private void queenMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves){
//
//        int row = myPosition.getRow();
//        int col = myPosition.getColumn();


        //horizontal movement
//        for (int c = col - 1; c >= 0; c--) {
//            if (!QBRmoves(board, myPosition, row, c, validMoves)){
//                break;
//            }
//        }
//        for (int c = col + 1; c < 8; c++) {
//            if (!QBRmoves(board, myPosition, row, c, validMoves)){
//                break;
//            }
//        }
//        for (int r = row - 1; r >= 0; r--) {
//            if (!QBRmoves(board, myPosition, r, col, validMoves)) {
//                break;
//            }
//        }
//        for (int r = row + 1; r < 8; r++) {
//            if (!QBRmoves(board, myPosition, r, col, validMoves)) {
//                break;
//            }
//        }


        //diagnol movement
//        for (int c = col - 1, r = row - 1; c >= 0 && r >= 0; c--, r--) {
//            if (!QBRmoves(board, myPosition, r, c, validMoves)){
//                break;
//            }
//        }
//        for (int c = col + 1, r = row - 1; c < 8 && r >= 0; c++, r--) {
//            if (!QBRmoves(board, myPosition, r, c, validMoves)){
//                break;
//            }
//        }
//        for (int c = col - 1, r = row + 1; c >= 0 && r < 8; c--, r++) {
//            if (!QBRmoves(board, myPosition, r, c, validMoves)) {
//                break;
//            }
//        }
//        for (int c = col + 1, r = row + 1; c < 8 && r < 8; c++, r++) {
//            if (!QBRmoves(board, myPosition, r, c, validMoves)) {
//                break;
//            }
//        }
//
//    }

    private void knightMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves){

        int[][] possibleMoves = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        for (int[] possibleMove : possibleMoves) {
            int endRow = row + possibleMove[0];
            int endCol = col + possibleMove[1];
            if (moveAble(endRow, endCol)){
                ChessPosition newPos = new ChessPosition(endRow, endCol);
                ChessPiece pieceAtNewPos = board.getPiece(newPos);

                if (pieceAtNewPos == null || theOpp(pieceAtNewPos)){
                    validMoves.add(new ChessMove(myPosition, newPos, null));
                }

            }
        }
    }

    private void kingMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMoves){
        int[][] possibleMoves = {
                {1, -1}, {1, 0}, {1, 1},
                {0, -1},         {0, 1},
                {-1, -1}, {-1, 0}, {-1, 1}
        };

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        for (int[] possibleMove : possibleMoves) {
            int endRow = row + possibleMove[0];
            int endCol = col + possibleMove[1];
            if (moveAble(endRow, endCol)){
                ChessPosition newPos = new ChessPosition(endRow, endCol);
                ChessPiece pieceAtNewPos = board.getPiece(newPos);

                if (pieceAtNewPos == null || theOpp(pieceAtNewPos)){
                    validMoves.add(new ChessMove(myPosition, newPos, null));
                }

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

    //QBRmoves is how the queen, bishop, and rook acts. if there is
    //an opposing piece, then it takes and cant go further. if there
    //is a team piece, it gets blocked.
    private boolean QBRmoves(ChessBoard board, ChessPosition startPos,
                             int endRow, int endCol, Collection<ChessMove> moves){

        if(!moveAble(endRow, endCol)){
            return false;
        }

        ChessPosition endPos = new ChessPosition(endRow, endCol);
        ChessPiece endPiece = board.getPiece(endPos);

        if (endPiece == null){
            moves.add(new ChessMove(startPos, endPos, null));
            return true;
        }
        else if (friendlyBlock(endPiece)){
            return false;
        }
        else if (theOpp(endPiece)){
            moves.add(new ChessMove(startPos, endPos, null));
            return false;
        }
        return true;

    }
}
