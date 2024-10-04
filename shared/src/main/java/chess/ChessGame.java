package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor currentTeamTurn;


    public ChessGame() {
        this.board = new ChessBoard();
        this.currentTeamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        //throw new RuntimeException("Not implemented");
        return this.currentTeamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {

        //throw new RuntimeException("Not implemented");
        this.currentTeamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        //throw new RuntimeException("Not implemented");
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : moves) {
            sim(move);
            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
            undo(move);
        }
        return validMoves;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        //throw new RuntimeException("Not implemented");

        boolean isTurn = getTeamTurn() == board.getTeamInTheLocation(move.getStartPosition());
        Collection<ChessMove> moves = validMoves(move.getStartPosition());

        if (!isTurn) {
            throw new InvalidMoveException();
        }
        if (!moves.contains(move)){
            throw new InvalidMoveException();
        }
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());
        if (move.getPromotionPiece() != null) {
            movingPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
        }

        board.addPiece(move.getEndPosition(), movingPiece);
        board.removePiece(move.getStartPosition());


        if (currentTeamTurn == TeamColor.WHITE) {
            currentTeamTurn = TeamColor.BLACK;
        } else {
            currentTeamTurn = TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        //throw new RuntimeException("Not implemented");
        ChessPosition kingPosition = findKingPosition(teamColor);

        TeamColor myOpp;
        if (teamColor == TeamColor.WHITE) {
            myOpp = TeamColor.BLACK;
        } else {
            myOpp = TeamColor.WHITE;
        }

        Collection<ChessPiece> opponentPieces = getTeamPieces(myOpp);

        for (ChessPiece piece : opponentPieces) {
            Collection<ChessMove> moves = piece.pieceMoves(board, findPiecePosition(piece));
            for (ChessMove move : moves) {
                if (move.getEndPosition().equals(kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ChessPosition findPiecePosition(ChessPiece piece) {
        for (int row = 0; row <= 8; row++) {
            for (int col = 0; col <= 8; col++) {
                if (board.getPiece(new ChessPosition(row, col)) == piece) {
                    return new ChessPosition(row, col);
                }
            }
        }
        return null;
    }
    private Collection<ChessPiece> getTeamPieces(TeamColor teamColor) {
        List<ChessPiece> pieces = new ArrayList<>();
        for (int row = 0; row <= 8; row++) {
            for (int col = 0; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null && piece.getTeamColor() == teamColor) {
                    pieces.add(piece);
                }
            }
        }
        return pieces;
    }
    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 0; row <= 8; row++) {
            for (int col = 0; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return new ChessPosition(row, col);
                }
            }
        }
        return null;
    }



    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        //throw new RuntimeException("Not implemented");
        if (!isInCheck(teamColor)){
            return false;
        }
        Collection<ChessPiece> teamPieces = getTeamPieces(teamColor);
        for (ChessPiece piece : teamPieces) {
            ChessPosition currentPosition = findPiecePosition(piece);
            Collection<ChessMove> moves = piece.pieceMoves(board, currentPosition);
            for (ChessMove move : moves) {
                sim(move);
                if (!isInCheck(teamColor)) {
                    undo(move);
                    return false;
                }
                undo(move);
            }
        }


        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        //throw new RuntimeException("Not implemented");
        if (isInCheck(teamColor)) {
            return false;
        }

        Collection<ChessPiece> teamPieces = getTeamPieces(teamColor);
        for (ChessPiece piece : teamPieces) {
            ChessPosition position = findPiecePosition(piece);
            Collection<ChessMove> moves = validMoves(position);
            if (moves != null && !moves.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void sim(ChessMove move) {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        board.removePiece(move.getStartPosition());
        board.addPiece(move.getEndPosition(), piece);
    }

    private void undo(ChessMove move) {
        ChessPiece piece = board.getPiece(move.getEndPosition());
        board.removePiece(move.getEndPosition());
        board.addPiece(move.getStartPosition(), piece);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        //throw new RuntimeException("Not implemented");
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        //throw new RuntimeException("Not implemented");
        return this.board;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTeamTurn == chessGame.currentTeamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTeamTurn);
    }
}
