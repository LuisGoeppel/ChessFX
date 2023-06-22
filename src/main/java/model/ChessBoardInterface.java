package model;

import java.util.List;

public interface ChessBoardInterface {

    //Input methods

    /**
     * Moves a piece from the start position of the given move to the end Position of the given move
     * The move is only executed if it is legal
     * @param move The move to be made
     * @return if the move was legal and could be executed
     */
    boolean movePiece(Move move);

    /**
     * Executes moves based on the standard Move Notation. An example would be:
     * 1.e4 e5
     * 2.ke2 ke7
     * ...
     * If one move turns out to be illegal, this move and every following move will be
     * discarded
     * @param moves The moves to be executed, given in the standard move notation
     * @return if all moves could be imported and were legal to play
     */
    boolean executeMoves(String moves);

    //Output methods

    /**
     * Returns the current ChessBoard as a 2 dimensional array of Chess pieces
     * @return
     */
    ChessPiece[][] getBoard();

    /**
     * Returns the winner of the game. If the game is not over yet 'NONE' will be returned
     * If no contestant could emerge victorious either 'DRAW' or 'STALEMATE' will be returned
     * @return The current winner of the game
     */
    ChessWinner getWinner();

    /**
     * Returns if the game is already over
     * @return if the game is over
     */
    boolean isGameOver();
    List<ChessPiece> getFallenPieces();

    /**
     * Returns the combined value of all fallen pieces of the given Color
     * @param color The color to get the value of
     * @return The combined value of all fallen pieces
     */
    int getFallenPiecesValue(ChessPieceColor color);

    /**
     * Returns the current player (Which Colors turn is it?)
     * @return the current player
     */
    ChessPieceColor getCurrentPlayer();

    /**
     * Returns a list of all legal moves in the current Position
     * @param chessPos The position of the piece to get the possible moves of
     * @return A list of all legal moves for the given piece
     */
    List<ChessPos> getPossibleMoves(ChessPos chessPos);

    /**
     * Returns a FEN Notation for the current position
     * @return a FEN Notation for the current position
     */
    String getAsFEN();

    /**
     * Returns a list of all legal moves in the current Position
     * @return a list of all legal moves in the current Position
     */
    List<String> getMoves();

    /**
     * Returns if a move is a promotion and should hence contain promotion input
     * @param move The possible promotion
     * @return if the move is a promotion
     */
    boolean isPromotionMove(Move move);

    /**
     * Returns the type of move on the current board
     *
     * @param move the move that will be made
     * @return if the move was a check, capture, etc..
     */
    MoveType getMoveType(Move move);
}
