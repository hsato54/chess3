package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private ChessPosition startPos;
    private ChessPosition endPos;
    private ChessPiece.PieceType promo;
//    private ChessGame.TeamColor team;
//    private ChessPiece.PieceType type;


    public ChessMove(ChessPosition startPos, ChessPosition endPos,
                     ChessPiece.PieceType promo) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.promo = promo;
//        this.team = team;
//        this.type = type;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {

        //throw new RuntimeException("Not implemented");
        return startPos;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {

        //throw new RuntimeException("Not implemented");
        return endPos;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {

        throw new RuntimeException("Not implemented");
//        if (type == ChessPiece.PieceType.PAWN){
//            if (team == ChessGame.TeamColor.WHITE && endPos.getRow() == 7){
//                return promo;
//            }
//            if (team == ChessGame.TeamColor.BLACK && endPos.getRow() == 0){
//                return promo;
//            }
//        }
//        return null;
//    }
    }
}
