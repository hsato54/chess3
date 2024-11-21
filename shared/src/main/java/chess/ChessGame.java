package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

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
        board.resetBoard();
        this.currentTeamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.currentTeamTurn;
    }

    /**
     * Sets which team's turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTeamTurn = team;
    }

    /**
     * Enum identifying the two possible teams in a chess game.
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets valid moves for a piece at the given location.
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for the requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : moves) {
            ChessPiece endPiece = board.getPiece(move.getEndPosition());
            sim(move);
            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
            undo(move, endPiece);
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game.
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        boolean isTurn = getTeamTurn() == board.getTeamInTheLocation(move.getStartPosition());
        Collection<ChessMove> moves = validMoves(move.getStartPosition());

        if (!isTurn) {
            throw new InvalidMoveException();
        }
        if (!moves.contains(move)) {
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
     * Determines if the given team is in check.
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);

        TeamColor opponent = teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;

        Collection<ChessPiece> opponentPieces = getTeamPieces(opponent);

        for (ChessPiece piece : opponentPieces) {
            Collection<ChessMove> moves = piece.pieceMoves(board, findPiece(piece));
            for (ChessMove move : moves) {
                if (move.getEndPosition().equals(kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ChessPosition findPiece(ChessPiece piece) {
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

    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 0; row <= 8; row++) {
            for (int col = 0; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null &&
                        piece.getPieceType() == ChessPiece.PieceType.KING &&
                        piece.getTeamColor() == teamColor) {
                    return new ChessPosition(row, col);
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate.
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        Collection<ChessPiece> teamPieces = getTeamPieces(teamColor);
        for (ChessPiece piece : teamPieces) {
            ChessPosition currentPosition = findPiece(piece);
            Collection<ChessMove> moves = piece.pieceMoves(board, currentPosition);
            for (ChessMove move : moves) {
                ChessPiece endPiece = board.getPiece(move.getEndPosition());
                sim(move);
                if (!isInCheck(teamColor)) {
                    undo(move, endPiece);
                    return false;
                }
                undo(move, endPiece);
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        Collection<ChessPiece> teamPieces = getTeamPieces(teamColor);
        for (ChessPiece piece : teamPieces) {
            ChessPosition position = findPiece(piece);
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

    private void undo(ChessMove move, ChessPiece endPiece) {
        ChessPiece piece = board.getPiece(move.getEndPosition());
        board.addPiece(move.getStartPosition(), piece);
        board.addPiece(move.getEndPosition(), endPiece);
    }

    /**
     * Sets this game's chessboard with a given board.
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard.
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) &&
                currentTeamTurn == chessGame.currentTeamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTeamTurn);
    }
}
