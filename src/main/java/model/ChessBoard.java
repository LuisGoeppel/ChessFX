package model;

import java.util.*;

@SuppressWarnings("DanglingJavadoc")
public class ChessBoard implements ChessBoardInterface {

    private final ChessPiece[][] board;
    private final Map<ChessPos, List<ChessPos>> moveOptions;
    private final List<ChessPiece> fallenPieces;
    private ChessPieceColor currentPlayer;
    private final List<String> moves;
    private String whiteMove;
    private ChessWinner winner;
    private ChessPos possibleEnPassant;
    private List<Character> castleOptions;
    private int halfMovesSinceLastPawnPushOrCapture;
    private int completedMoves;
    private boolean isGameOver;
    public static final int BOARD_SIZE = 8;
    private static final String DEFAULT_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public static final HashMap<ChessPieceType, Integer> pieceValues = new HashMap<>(Map.of(
            ChessPieceType.PAWN, 1,
            ChessPieceType.KNIGHT, 3,
            ChessPieceType.BISHOP, 3,
            ChessPieceType.ROOK, 5,
            ChessPieceType.QUEEN, 9,
            ChessPieceType.KING, Integer.MAX_VALUE
    ));


    /** -------------------------------------------------------------------------------------------------- **/
    /**                                              CONSTRUCTOR                                           **/
    /** -------------------------------------------------------------------------------------------------- **/
    public ChessBoard() {
        this(DEFAULT_POSITION);
    }

    public ChessBoard(String FEN) {
        String[] arguments = FEN.split(" ");
        board = getPosition(arguments[0]);
        if (arguments.length >= 2) {
            currentPlayer = getNextPlayer(arguments[1]);
            if (arguments.length >= 3) {
                castleOptions = getCastleOptions(arguments[2]);
                if (arguments.length >= 4) {
                    if (!arguments[3].equals("-")) {
                        possibleEnPassant = new ChessPos(arguments[3]);
                    }
                    if (arguments.length >= 6) {
                        halfMovesSinceLastPawnPushOrCapture = Integer.parseInt(arguments[4]);
                        completedMoves = Integer.parseInt(arguments[5]);
                    } else {
                        halfMovesSinceLastPawnPushOrCapture = 0;
                        completedMoves = 0;
                    }
                }
            } else {
                castleOptions = new ArrayList<>();
            }
        } else {
            currentPlayer = ChessPieceColor.WHITE;
        }
        isGameOver = false;
        fallenPieces = new ArrayList<>();
        moveOptions = new HashMap<>();
        moves = new ArrayList<>();
        winner = ChessWinner.NONE;
    }

    /** -------------------------------------------------------------------------------------------------- **/
    /**                                           PUBLIC METHODS                                           **/
    /** -------------------------------------------------------------------------------------------------- **/

    /** ----------------------------------------- MOVE METHODS ------------------------------------------- **/
    @Override
    public boolean movePiece(Move move) {
        if (!isGameOver) {
            int xFrom = move.from.getAsCoordinates()[0];
            int yFrom = move.from.getAsCoordinates()[1];
            int xTo = move.to.getAsCoordinates()[0];
            int yTo = move.to.getAsCoordinates()[1];

            ChessPieceColor c = board[xFrom][yFrom].getColor();
            if(!c.equals(currentPlayer)) {
                return false;
            }
            if(board[xFrom][yFrom].getPieceType().equals(ChessPieceType.PAWN)
                    && (yTo == 0 || yTo == 7) && !move.to.hasPromotionInput()) {
                return false;
            }
            if (!moveOptions.containsKey(move.from)) {
                getPossibleMoves(move.from);
            }
            List<ChessPos> possibleMoves = moveOptions.get(move.from);
            if (possibleMoves == null || !possibleMoves.contains(move.to)) {
                return false;
            }

            //Let's make the move
            handleMoveCounters(xFrom, yFrom, xTo, yTo);
            boolean moveIsCapture = isCapture(move);

            ChessPiece piece = board[xFrom][yFrom];
            ChessBoard beforeMove = clone();
            board[xFrom][yFrom] = new ChessPiece();
            if (move.to.hasPromotionInput()) {
                board[xTo][yTo] = new ChessPiece(move.to.getPromoteTo(), currentPlayer);
            } else {
                board[xTo][yTo] = piece;
            }
            if (move.to.equals(possibleEnPassant)) {
                int direction = currentPlayer.equals(ChessPieceColor.WHITE) ? 1: -1;
                board[xTo][yTo + direction] = new ChessPiece();
            }

            currentPlayer = currentPlayer.equals(ChessPieceColor.BLACK) ? ChessPieceColor.WHITE : ChessPieceColor.BLACK;
            handlePossibleCastling(piece, xFrom, yFrom, xTo, yTo);
            moveOptions.clear();

            if (!checkGameOver()) {
                handleEnPassantOptions(piece, xTo, yTo);
                handleCastleOptions(piece, move.from);
            }

            saveMove(move, beforeMove, moveIsCapture);

            return true;
        }
        return false;
    }

    @Override
    public boolean executeMoves(String moves) {

        String[] movesAsString = moves.split(" ");
        for (int i = 0; i < movesAsString.length; i++) {
            if (i % 3 != 0) {
                if (!executeMove(movesAsString[i])) {
                    throw new IllegalArgumentException("Move " + movesAsString[i] + " could not be processed!");
                }
            }
        }
        return true;
    }

    /** -------------------------------------- GET POSSIBLE MOVES ---------------------------------------- **/
    @Override
    public List<ChessPos> getPossibleMoves(ChessPos chessPos) {
        if (moveOptions.containsKey(chessPos)) {
            return moveOptions.get(chessPos);
        }

        int xCord = chessPos.getAsCoordinates()[0];
        int yCord = chessPos.getAsCoordinates()[1];

        ChessPiece piece = board[xCord][yCord];
        List<ChessPos> possibleMoves;
        if (!piece.getColor().equals(currentPlayer)) {
            return new ArrayList<>();
        }
        switch (piece.getPieceType()) {
            case ROOK:
                possibleMoves = getRookMoves(chessPos);
                break;
            case BISHOP:
                possibleMoves = getBishopMoves(chessPos);
                break;
            case QUEEN:
                possibleMoves = getRookMoves(chessPos);
                possibleMoves.addAll(getBishopMoves(chessPos));
                break;
            case KING:
                possibleMoves = getKingMoves(chessPos);
                break;
            case KNIGHT:
                possibleMoves = getKnightMoves(chessPos);
                break;
            case PAWN:
                possibleMoves = getPawnMoves(chessPos);
                break;
            default:
                possibleMoves = new ArrayList<>();
        }
        List<ChessPos> possibleMovesUpdated = new ArrayList<>();
        for (ChessPos pos : possibleMoves) {
            ChessBoard clonedBoard = new ChessBoard(getAsFEN());
            clonedBoard.board[xCord][yCord] = new ChessPiece(ChessPieceType.EMPTY, ChessPieceColor.EMPTY);
            clonedBoard.board[pos.getAsCoordinates()[0]][pos.getAsCoordinates()[1]] = piece;
            if (!clonedBoard.isKingInCheck(piece.getColor())) {
                possibleMovesUpdated.add(pos);
            }
        }
        if (!possibleMovesUpdated.isEmpty()) {
            moveOptions.put(chessPos, possibleMovesUpdated);
        }
        return possibleMovesUpdated;
    }

    public List<Move> getPossibleMoves() {
        List<Move> possibleMoves = new ArrayList<>();
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (board[x][y].getColor().equals(currentPlayer)) {
                    ChessPos startPos = new ChessPos(x, y);
                    List<ChessPos> pieceMoves = getPossibleMoves(startPos);
                    for (ChessPos endPos : pieceMoves) {
                        possibleMoves.add(new Move(startPos, endPos));
                    }
                }
            }
        }
        return possibleMoves;
    }

    /** ------------------------------------------- GET AS FEN -------------------------------------------- **/
    @Override
    public String getAsFEN() {
        StringBuilder positionBuilder = new StringBuilder();
        StringBuilder FENBuilder = new StringBuilder();
        int nEmptyFields = 0;
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            char c = board[i % BOARD_SIZE][i / BOARD_SIZE].getAsChar();
            if (c == 'E') {
                nEmptyFields++;
            } else {
                if (nEmptyFields > 0) {
                    positionBuilder.append(nEmptyFields);
                    nEmptyFields = 0;
                }
                positionBuilder.append(c);
            }
            if ((i + 1) % BOARD_SIZE == 0) {
                if (nEmptyFields > 0) {
                    positionBuilder.append(nEmptyFields);
                    nEmptyFields = 0;
                }
                positionBuilder.append('/');
            }
        }
        FENBuilder.append(positionBuilder.substring(0, positionBuilder.length() - 1)).append(" ");
        char nextPlayer = currentPlayer.equals(ChessPieceColor.WHITE) ? 'w' : 'b';
        FENBuilder.append(nextPlayer).append(" ");
        for (Character castleOption : castleOptions) {
            FENBuilder.append(castleOption);
        }
        if (castleOptions.size() == 0) {
            FENBuilder.append("-");
        }
        FENBuilder.append(" ");
        if (possibleEnPassant != null) {
            FENBuilder.append(possibleEnPassant).append(" ");
        } else {
            FENBuilder.append("- ");
        }
        FENBuilder.append(halfMovesSinceLastPawnPushOrCapture).append(" ").append(completedMoves);

        return FENBuilder.toString();
    }

    /** ---------------------------------------- TO-STRING AND CLONE ----------------------------------------- **/
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            output.append(getPieceRepresentation(board[i % BOARD_SIZE][i / BOARD_SIZE])).append(" ");
            if ((i + 1) % BOARD_SIZE == 0) {
                output.append("\n");
            }
        }
        return output.toString();
    }

    @Override
    public ChessBoard clone() {
        return new ChessBoard(getAsFEN());
    }

    /** ----------------------------------------- SIMPLE GETTER ------------------------------------------ **/
    @Override
    public ChessPiece[][] getBoard() {
        return board;
    }

    @Override
    public ChessWinner getWinner() {
        return winner;
    }

    @Override
    public boolean isGameOver() {
        return isGameOver;
    }

    @Override
    public List<String> getMoves() {
        return moves;
    }

    @Override
    public boolean isPromotionMove(Move move) {
        int xFrom = move.from.getAsCoordinates()[0];
        int yFrom = move.from.getAsCoordinates()[1];
        int yTo = move.to.getAsCoordinates()[1];

        if (board[xFrom][yFrom].getPieceType().equals(ChessPieceType.PAWN)) {
            if (!isMovementPossible(move) || !board[xFrom][yFrom].getColor().equals(currentPlayer)) {
                return false;
            }
            if (board[xFrom][yFrom].getColor().equals(ChessPieceColor.WHITE)) {
                return yTo == 0;
            } else {
                return yTo == 7;
            }
        }
        return false;
    }

    @Override
    public List<ChessPiece> getFallenPieces() {
        return fallenPieces;
    }

    @Override
    public int getFallenPiecesValue(ChessPieceColor color) {
        int fallenPiecesValue = 0;
        for (ChessPiece piece : fallenPieces) {
            if (piece.getColor().equals(color)) {
                fallenPiecesValue += pieceValues.get(piece.getPieceType());
            }
        }
        return fallenPiecesValue;
    }

    @Override
    public ChessPieceColor getCurrentPlayer() {
        return currentPlayer;
    }

    /** -------------------------------------------------------------------------------------------------- **/
    /**                                           PRIVATE METHODS                                          **/
    /** -------------------------------------------------------------------------------------------------- **/

    /** ---------------------------------------- GET MOVE METHODS ---------------------------------------- **/
    private List<ChessPos> getPawnMoves(ChessPos chessPos) {
        int xCord = chessPos.getAsCoordinates()[0];
        int yCord = chessPos.getAsCoordinates()[1];
        List<ChessPos> possibleMoves = new ArrayList<>();
        ChessPieceColor c = board[xCord][yCord].getColor();
        int direction = c.equals(ChessPieceColor.BLACK) ? 1 : -1;
        int startRank = c.equals(ChessPieceColor.BLACK) ? 1 : 6;
        int promotionRank = c.equals(ChessPieceColor.BLACK) ? 7 : 0;

        //Forward move
        if (inRange(xCord, yCord + direction) && !board[xCord][yCord + direction].hasPiece()) {
            if (yCord + direction == promotionRank) {
                possibleMoves.add(new ChessPos(xCord, yCord + direction, ChessPieceType.QUEEN));
                possibleMoves.add(new ChessPos(xCord, yCord + direction, ChessPieceType.ROOK));
                possibleMoves.add(new ChessPos(xCord, yCord + direction, ChessPieceType.BISHOP));
                possibleMoves.add(new ChessPos(xCord, yCord + direction, ChessPieceType.KNIGHT));
            } else {
                possibleMoves.add(new ChessPos(xCord, yCord + direction));
            }
        }

        //Move forward by two fields, if the pawn is on the start rank
        if (inRange(xCord, yCord + direction, yCord + direction * 2) && yCord == startRank &&
                !board[xCord][yCord + direction].hasPiece() && !board[xCord][yCord + direction * 2].hasPiece()) {
            possibleMoves.add(new ChessPos(xCord, yCord + direction * 2));
        }

        //Capture a piece to the left of the pawn
        if (inRange(xCord - 1, yCord + direction) && (board[xCord - 1][yCord + direction].isEnemyColorTo(c)
                || (possibleEnPassant != null && possibleEnPassant.equals(new ChessPos(xCord - 1, yCord + direction))))) {
            if (yCord + direction == promotionRank) {
                possibleMoves.add(new ChessPos(xCord - 1, yCord + direction, ChessPieceType.QUEEN));
                possibleMoves.add(new ChessPos(xCord - 1, yCord + direction, ChessPieceType.ROOK));
                possibleMoves.add(new ChessPos(xCord - 1, yCord + direction, ChessPieceType.BISHOP));
                possibleMoves.add(new ChessPos(xCord - 1, yCord + direction, ChessPieceType.KNIGHT));
            } else {
                possibleMoves.add(new ChessPos(xCord - 1, yCord + direction));
            }
        }

        //Capture a piece to the right of the pawn
        if (inRange(xCord + 1, yCord + direction) && (board[xCord + 1][yCord + direction].isEnemyColorTo(c) ||
                (possibleEnPassant != null && possibleEnPassant.equals(new ChessPos(xCord + 1, yCord + direction))))) {
            if (yCord + direction == promotionRank) {
                possibleMoves.add(new ChessPos(xCord + 1, yCord + direction, ChessPieceType.QUEEN));
                possibleMoves.add(new ChessPos(xCord + 1, yCord + direction, ChessPieceType.ROOK));
                possibleMoves.add(new ChessPos(xCord + 1, yCord + direction, ChessPieceType.BISHOP));
                possibleMoves.add(new ChessPos(xCord + 1, yCord + direction, ChessPieceType.KNIGHT));
            } else {
                possibleMoves.add(new ChessPos(xCord + 1, yCord + direction));
            }
        }
        return possibleMoves;
    }

    private List<ChessPos> getKnightMoves(ChessPos chessPos) {
        int xCord = chessPos.getAsCoordinates()[0];
        int yCord = chessPos.getAsCoordinates()[1];
        List<ChessPos> possibleMoves = new ArrayList<>();
        ChessPieceColor c = board[xCord][yCord].getColor();

        for (int x = Math.max(0, xCord - 2); x <= Math.min(BOARD_SIZE - 1, xCord + 2); x++) {
            for (int y = Math.max(0, yCord - 2); y <= Math.min(BOARD_SIZE - 1, yCord + 2); y++) {
                if (Math.abs(xCord - x) + Math.abs(yCord - y) == 3 && (board[x][y].isEnemyColorTo(c) || !board[x][y].hasPiece())) {
                    possibleMoves.add(new ChessPos(x, y));
                }
            }
        }
        return possibleMoves;
    }

    private List<ChessPos> getBishopMoves(ChessPos chessPos) {
        int xCord = chessPos.getAsCoordinates()[0];
        int yCord = chessPos.getAsCoordinates()[1];
        List<ChessPos> possibleMoves = new ArrayList<>();
        ChessPieceColor c = board[xCord][yCord].getColor();

        addMoves(possibleMoves, xCord, yCord, c, 1, 1);
        addMoves(possibleMoves, xCord, yCord, c, 1, -1);
        addMoves(possibleMoves, xCord, yCord, c, -1, 1);
        addMoves(possibleMoves, xCord, yCord, c, -1, -1);

        return possibleMoves;
    }

    private List<ChessPos> getRookMoves(ChessPos chessPos) {
        int xCord = chessPos.getAsCoordinates()[0];
        int yCord = chessPos.getAsCoordinates()[1];
        List<ChessPos> possibleMoves = new ArrayList<>();
        ChessPieceColor c = board[xCord][yCord].getColor();

        addMoves(possibleMoves, xCord, yCord, c, 1, 0);
        addMoves(possibleMoves, xCord, yCord, c, -1, 0);
        addMoves(possibleMoves, xCord, yCord, c, 0, 1);
        addMoves(possibleMoves, xCord, yCord, c, 0, -1);

        return possibleMoves;
    }

    private List<ChessPos> getKingMoves(ChessPos chessPos) {
        int xCord = chessPos.getAsCoordinates()[0];
        int yCord = chessPos.getAsCoordinates()[1];
        List<ChessPos> possibleMoves = new ArrayList<>();
        ChessPieceColor c = board[xCord][yCord].getColor();

        for (int x = Math.max(0, xCord - 1); x <= Math.min(BOARD_SIZE - 1, xCord + 1); x++) {
            for (int y = Math.max(0, yCord - 1); y <= Math.min(BOARD_SIZE - 1, yCord + 1); y++) {
                if (!board[x][y].hasPiece() || board[x][y].isEnemyColorTo(c)) {
                    possibleMoves.add(new ChessPos(x, y));
                }
            }
        }
        // Castle Options
        char castleKingSide = c.equals(ChessPieceColor.BLACK) ? 'k' : 'K';
        char castleQueenSide = c.equals(ChessPieceColor.BLACK) ? 'q' : 'Q';
        if (castleOptions.contains(castleKingSide) && !board[xCord + 1][yCord].hasPiece() && !board[xCord + 2][yCord].hasPiece()) {
            possibleMoves.add(new ChessPos(xCord + 2, yCord));
        }
        if (castleOptions.contains(castleQueenSide) && !board[xCord - 1][yCord].hasPiece()
                && !board[xCord - 2][yCord].hasPiece() && !board[xCord - 3][yCord].hasPiece()) {
            possibleMoves.add(new ChessPos(xCord - 2, yCord));
        }
        return possibleMoves;
    }

    /** ---------------------------------- CHECK IF MOVES ARE LEGAL ----------------------------------- **/

    private boolean executeMove(String move) {
        move = move.replaceAll("[x+#]", "");

        //Normal pawn move
        if (move.length() == 2) {
            ChessPos to = new ChessPos(move);
            int direction = currentPlayer.equals(ChessPieceColor.BLACK) ? 1 : -1;
            if (board[to.getAsCoordinates()[0]][to.getAsCoordinates()[1] - direction].hasPiece()) {
                ChessPos from = new ChessPos(to.getAsCoordinates()[0], to.getAsCoordinates()[1] - direction);
                return movePiece(new Move(from, to));
            } else if (board[to.getAsCoordinates()[0]][to.getAsCoordinates()[1] - 2 * direction].hasPiece()) {
                ChessPos from = new ChessPos(to.getAsCoordinates()[0], to.getAsCoordinates()[1] - 2 * direction);
                return movePiece(new Move(from, to));
            }

            //Castle Moves
        } else if (move.equals("O-O")) {
            int rank = currentPlayer.equals(ChessPieceColor.WHITE) ? 1 : 8;
            return board[4][8 - rank].getPieceType().equals(ChessPieceType.KING)
                    && movePiece(new Move("e" + rank, "g" + rank));
        } else if (move.equals("O-O-O")) {
            int rank = currentPlayer.equals(ChessPieceColor.WHITE) ? 1 : 8;
            return board[4][8 - rank].getPieceType().equals(ChessPieceType.KING)
                    && movePiece(new Move("e" + rank, "c" + rank));

            //Promotion Input
        } else if (move.contains("=")) {
            String[] inputs = move.split("=");
            int direction = currentPlayer.equals(ChessPieceColor.BLACK) ? 1 : -1;
            ChessPieceType promoteTo = ChessPieceType.valueOf(ChessPieceType.getEnumByString(inputs[1]));
            ChessPos from, to;
            if (inputs[0].length() == 2) {
                to = new ChessPos(inputs[0], promoteTo);
                from = new ChessPos(to.getAsCoordinates()[0], to.getAsCoordinates()[1] - direction);
            } else {
                to = new ChessPos(inputs[0].substring(1), promoteTo);
                char firstChar = inputs[0].charAt(0);
                from = new ChessPos(to.getAsCoordinates()[0] + ((firstChar - 'a') - to.getColumn()),
                        to.getAsCoordinates()[1] - direction);
            }
            return movePiece(new Move(from, to));

            //Default Piece Movement
        } else {
            char pieceChar = move.charAt(0);
            ChessPos to = new ChessPos(move.substring(move.length() - 2));

            if (Character.isLowerCase(pieceChar)) {
                int directionY = currentPlayer.equals(ChessPieceColor.BLACK) ? 1 : -1;
                ChessPos from = new ChessPos(pieceChar,8 - (to.getAsCoordinates()[1] - directionY));
                return movePiece(new Move(from, to));
            } else {
                ChessPieceType pieceType = ChessPieceType.valueOf(ChessPieceType.
                        getEnumByString(Character.toString(pieceChar)));
                List<ChessPos> from = searchPiece(new ChessPiece(pieceType, currentPlayer));
                if (from.isEmpty()) {
                    throw new IllegalArgumentException("Move " + move + "is not legal");
                }
                if (from.size() == 1) {
                    return movePiece(new Move(from.get(0), to));
                } else {
                    if (move.length() == 3) {
                        return movePiece(new Move(from.get(0), to)) || movePiece(new Move(from.get(1), to));
                    }
                    char posDetail = move.charAt(1);
                    if ((Character.isDigit(posDetail) && from.get(0).getAsCoordinates()[1] == posDetail - '0')
                            || (posDetail - 'a' < 8 && from.get(0).getAsCoordinates()[0] == posDetail - 'a')) {
                        return movePiece(new Move(from.get(0), to));
                    } else {
                        return movePiece(new Move(from.get(1), to));
                    }
                }
            }
        }
        return false;
    }

    private boolean pieceMovementPossible(ChessPieceColor chessPieceColor) {
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            if (board[i % BOARD_SIZE][i / BOARD_SIZE].getColor().equals(chessPieceColor)) {
                List<ChessPos> possibleMoves = getPossibleMoves(new ChessPos(i % BOARD_SIZE, i / BOARD_SIZE));
                if (possibleMoves.size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addMoves(List<ChessPos> possibleMoves, int xCord, int yCord, ChessPieceColor c, int xDir, int yDir) {
        int i = 1;
        while (inRange(xCord + i * xDir, yCord + i * yDir)
                && !board[xCord + i * xDir][yCord + i * yDir].hasPiece()) {
            possibleMoves.add(new ChessPos(xCord + i * xDir, yCord + i * yDir));
            i++;
        }
        if (inRange(xCord + i * xDir, yCord + i * yDir)
                && board[xCord + i * xDir][yCord + i * yDir].isEnemyColorTo(c)) {
            possibleMoves.add(new ChessPos(xCord + i * xDir, yCord + i * yDir));
        }
    }

    private void handlePossibleCastling(ChessPiece piece, int xFrom, int yFrom, int xTo, int yTo) {
        if (piece.getPieceType().equals(ChessPieceType.KING) && Math.abs(xFrom - xTo) > 1) {
            boolean isKingSideCastle = xFrom < xTo;
            if (isKingSideCastle) {
                board[xFrom + 1][yFrom] = board[xFrom + 3][yFrom];
                board[xTo + 1][yTo] = new ChessPiece(ChessPieceType.EMPTY, ChessPieceColor.EMPTY);
            } else {
                board[xFrom - 1][yFrom] = board[xFrom - 4][yFrom];
                board[xTo - 2][yTo] = new ChessPiece(ChessPieceType.EMPTY, ChessPieceColor.EMPTY);
            }
        }
    }

    @SuppressWarnings("DuplicateExpressions")
    private boolean isMovementPossible(Move move) {
        if (move.from.equals(move.to)) {
            return false;
        }
        int xFrom = move.from.getAsCoordinates()[0];
        int yFrom = move.from.getAsCoordinates()[1];
        int xTo = move.to.getAsCoordinates()[0];
        int yTo = move.to.getAsCoordinates()[1];

        ChessPiece chessPiece = board[xFrom][yFrom];

        switch (chessPiece.getPieceType()) {
            case KING:
                return Math.abs(xFrom - xTo) <= 1 && Math.abs(yFrom - yTo) <= 1;
            case QUEEN:
                return ((xFrom == xTo || yFrom == yTo) || (Math.abs(xFrom - xTo) == Math.abs(yFrom - yTo)))
                        && inBetweenFieldsEmpty(move) && (isCapture(move) || !board[xTo][yTo].hasPiece());
            case ROOK:
                return (xFrom == xTo || yFrom == yTo) && inBetweenFieldsEmpty(move) && (isCapture(move)
                        || !board[xTo][yTo].hasPiece());
            case BISHOP:
                return (Math.abs(xFrom - xTo) == Math.abs(yFrom - yTo)) && inBetweenFieldsEmpty(move)
                        && (isCapture(move) || !board[xTo][yTo].hasPiece());
            case KNIGHT:
                return ((Math.abs(xFrom - xTo) == 1 && Math.abs(yFrom - yTo) == 2)
                        || (Math.abs(xFrom - xTo) == 2 && Math.abs(yFrom - yTo) == 1))
                        && (isCapture(move) || !board[xTo][yTo].hasPiece());
            case PAWN:
                int colorFactor = chessPiece.getColor().equals(ChessPieceColor.WHITE) ? -1 : 1;
                return Math.abs(xFrom - xTo) == 1 && yFrom + colorFactor == yTo
                        && board[xTo][yTo].isEnemyColorTo(chessPiece.getColor());
            default:
                return false;
        }
    }

    private boolean isKingInCheck(ChessPieceColor color) {
        ChessPos kingPosition = null;
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            if (board[i % BOARD_SIZE][i / BOARD_SIZE].getPieceType().equals(ChessPieceType.KING)
                    && board[i % BOARD_SIZE][i / BOARD_SIZE].getColor().equals(color)) {
                kingPosition = new ChessPos(i % BOARD_SIZE, i / BOARD_SIZE);
                break;
            }
        }
        if (kingPosition == null) {
            throw new IllegalArgumentException("King could not be found");
        }

        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            if (board[i % BOARD_SIZE][i / BOARD_SIZE].isEnemyColorTo(color)) {
                ChessPos piecePosition = new ChessPos(i % BOARD_SIZE, i / BOARD_SIZE);
                if (isMovementPossible(new Move(piecePosition, kingPosition))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean inBetweenFieldsEmpty(Move move) {
        int xFrom = move.from.getAsCoordinates()[0];
        int yFrom = move.from.getAsCoordinates()[1];
        int xTo = move.to.getAsCoordinates()[0];
        int yTo = move.to.getAsCoordinates()[1];

        if (xFrom == xTo) {
            for (int i = Math.min(yFrom, yTo) + 1; i < Math.max(yFrom, yTo); i++) {
                if (board[xFrom][i].hasPiece()) {
                    return false;
                }
            }
            return true;
        }
        if (yFrom == yTo) {
            for (int i = Math.min(xFrom, xTo) + 1; i < Math.max(xFrom, xTo); i++) {
                if (board[i][yFrom].hasPiece()) {
                    return false;
                }
            }
            return true;
        }
        int directionX = xFrom <= xTo ? 1 : -1;
        int directionY = yFrom <= yTo ? 1 : -1;
        for (int i = 1; i < Math.abs(xFrom - xTo); i++) {
            if (board[xFrom + i * directionX][yFrom + i * directionY].hasPiece()) {
                return false;
            }
        }
        return true;
    }

    /** ---------------------------------- CONSTRUCTOR HELPER METHODS ----------------------------------- **/
    private ChessPiece[][] getPosition(String input) {
        String boardPosition = replaceNumbersWithEmptyFields(input);
        ChessPiece[][] board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        if (boardPosition.length() != BOARD_SIZE * BOARD_SIZE) {
            throw new IllegalArgumentException("Invalid input: " + boardPosition.length()
                    + " and " + BOARD_SIZE * BOARD_SIZE + " don't match!");
        }
        for (int i = 0; i < boardPosition.length(); i++) {
            char currentChar = boardPosition.charAt(i);
            String currentString = String.valueOf(currentChar);
            ChessPieceType chessPieceType = ChessPieceType.valueOf(ChessPieceType
                    .getEnumByString(currentString.toUpperCase()));
            ChessPieceColor chessPieceColor = chessPieceType.equals(ChessPieceType.EMPTY) ? ChessPieceColor.EMPTY
                    : Character.isUpperCase(currentChar) ? ChessPieceColor.WHITE : ChessPieceColor.BLACK;
            board[i % BOARD_SIZE][i / BOARD_SIZE] = new ChessPiece(chessPieceType, chessPieceColor);
        }
        return board;
    }

    private ChessPieceColor getNextPlayer(String input) {
        if (input.equals("b")) {
            return ChessPieceColor.BLACK;
        }
        return ChessPieceColor.WHITE;
    }

    private List<Character> getCastleOptions(String input) {
        if (input.equals("-")) {
            return new ArrayList<>();
        }
        List<Character> validOptions = Arrays.asList('K', 'Q', 'k', 'q');
        List<Character> output = new ArrayList<>();
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (!validOptions.contains(current)) {
                throw new IllegalArgumentException("There is something wrong with the castle options!");
            }
            output.add(current);
        }
        return output;
    }

    /** ----------------------------------- UPDATE BOARD INFORMATION ------------------------------------ **/

    private boolean checkGameOver() {
        if (!pieceMovementPossible(currentPlayer)) {
            if (isKingInCheck(currentPlayer)) {
                winner = currentPlayer.equals(ChessPieceColor.BLACK) ? ChessWinner.WHITE : ChessWinner.BLACK;
            } else {
                winner = ChessWinner.STALEMATE;
            }
            isGameOver = true;
            return true;
        }
        return false;
    }

    private void saveMove(Move move, ChessBoard beforeMove, boolean moveIsCapture) {
        int xFrom = move.from.getAsCoordinates()[0];
        int yFrom = move.from.getAsCoordinates()[1];
        int xTo = move.to.getAsCoordinates()[0];
        int yTo = move.to.getAsCoordinates()[1];

        boolean moveIsCheck = isKingInCheck(currentPlayer);
        boolean moveIsCheckMate = isGameOver && moveIsCheck;
        ChessPieceColor moveMadeBy = board[xTo][yTo].getColor();
        int indexShift = 1;

        String halfMoveNotation;

        switch (beforeMove.board[xFrom][yFrom].getPieceType()) {
            case KING:
                if (yFrom == yTo && xFrom + 2 == xTo) {
                    halfMoveNotation = "O-O";
                } else if (yFrom == yTo && xFrom - 2 == xTo) {
                    halfMoveNotation = "O-O-O";
                } else {
                    halfMoveNotation = "K" + move.to;
                }
                break;
            case ROOK:
                List<ChessPos> rookPositions = beforeMove.searchPiece(new ChessPiece(ChessPieceType.ROOK, moveMadeBy));

                if (rookPositions.size() > 1 && beforeMove.isMovementPossible(new Move(rookPositions.get(0), move.to))
                        && beforeMove.isMovementPossible(new Move(rookPositions.get(1), move.to))) {
                    if (rookPositions.get(0).getAsCoordinates()[1] == rookPositions.get(1).getAsCoordinates()[1]) {
                        halfMoveNotation = "R" + ((char) (xFrom + 'a')) + move.to;
                    } else {
                        halfMoveNotation = "R" + (8 - yFrom) + move.to;
                    }
                    indexShift++;
                } else {
                    halfMoveNotation = "R" + move.to;
                }
                break;
            case KNIGHT:
                List<ChessPos> knightPositions = beforeMove.searchPiece(new ChessPiece(ChessPieceType.KNIGHT, moveMadeBy));
                if (knightPositions.size() > 1 && beforeMove.isMovementPossible(new Move(knightPositions.get(0), move.to))
                        && beforeMove.isMovementPossible(new Move(knightPositions.get(1), move.to))) {
                    if (knightPositions.get(0).getAsCoordinates()[1] == knightPositions.get(1).getAsCoordinates()[1]) {
                        halfMoveNotation = "N" + (8 - yFrom) + move.to;
                    } else {
                        halfMoveNotation = "N" + ((char) (xFrom + 'a')) + move.to;
                    }
                    indexShift++;
                } else {
                    halfMoveNotation = "N" + move.to;
                }
                break;
            case BISHOP:
                halfMoveNotation = "B" + move.to;
                break;
            case QUEEN:
                halfMoveNotation = "Q" + move.to;
                break;
            case PAWN:
                if (moveIsCapture) {
                    halfMoveNotation = move.from.toString().charAt(0) + move.to.toString();
                } else {
                    halfMoveNotation = move.to.toString();
                }
                break;
            default:
                halfMoveNotation = "ERROR";
                break;
        }
        if (moveIsCapture) {
            halfMoveNotation = halfMoveNotation.substring(0, indexShift) + "x" + halfMoveNotation.substring(1);
        }
        if (moveIsCheckMate) {
            halfMoveNotation = halfMoveNotation + "#";
        } else if (moveIsCheck) {
            halfMoveNotation = halfMoveNotation + "+";
        }

        if (moveMadeBy.equals(ChessPieceColor.WHITE)) {
            whiteMove = halfMoveNotation;
            if (isGameOver) {
                moves.add(completedMoves + ". " + whiteMove);
            }
        } else {
            moves.add((completedMoves - 1) + ". " + whiteMove + " " + halfMoveNotation);
        }
    }

    private void handleMoveCounters(int xFrom, int yFrom, int xTo, int yTo) {
        if (board[xTo][yTo].hasPiece()) {
            fallenPieces.add(board[xTo][yTo]);
        }
        if (currentPlayer.equals(ChessPieceColor.BLACK)) {
            completedMoves++;
        }
        if (!board[xFrom][yFrom].getPieceType().equals(ChessPieceType.PAWN) && !board[xTo][yTo].hasPiece()) {
            halfMovesSinceLastPawnPushOrCapture++;
            if (halfMovesSinceLastPawnPushOrCapture >= 50) {
                winner = ChessWinner.DRAW;
                isGameOver = true;
            }
        } else {
            halfMovesSinceLastPawnPushOrCapture = 0;
        }
    }

    private void handleEnPassantOptions(ChessPiece piece, int xTo, int yTo) {
        possibleEnPassant = null;
        if (piece.getPieceType().equals(ChessPieceType.PAWN)) {
            int rank = piece.getColor().equals(ChessPieceColor.BLACK) ? 3 : 4;
            if (yTo == rank
                    && ((xTo - 1 >= 0 && board[xTo - 1][yTo].isEnemyColorTo(piece.getColor())
                    && board[xTo - 1][yTo].getPieceType().equals(ChessPieceType.PAWN))
                    || (xTo + 1 < BOARD_SIZE && board[xTo + 1][yTo].isEnemyColorTo(piece.getColor())
                    && board[xTo + 1][yTo].getPieceType().equals(ChessPieceType.PAWN)))) {
                int direction = piece.getColor().equals(ChessPieceColor.BLACK) ? -1 : 1;
                possibleEnPassant = new ChessPos(xTo, yTo + direction);
            }
        }
    }

    private void handleCastleOptions(ChessPiece piece, ChessPos from) {
        if (piece.getPieceType().equals(ChessPieceType.KING)) {
            if (piece.getColor().equals(ChessPieceColor.BLACK)) {
                castleOptions.remove(new Character('k'));
                castleOptions.remove(new Character('q'));
            } else {
                castleOptions.remove(new Character('K'));
                castleOptions.remove(new Character('Q'));
            }
        }
        if (piece.getPieceType().equals(ChessPieceType.ROOK)) {
            if (from.equals(new ChessPos("a8"))) {
                castleOptions.removeIf(element -> element == 'q');
            } else if (from.equals(new ChessPos("h8"))) {
                castleOptions.removeIf(element -> element == 'k');
            } else if (from.equals(new ChessPos("a1"))) {
                castleOptions.removeIf(element -> element == 'Q');
            } else if (from.equals(new ChessPos("h1"))) {
                castleOptions.removeIf(element -> element == 'K');
            }
        }
    }

    /** --------------------------------------- HELPER METHODS ------------------------------------------ **/

    private List<ChessPos> searchPiece(ChessPiece piece) {
        if (piece.getPieceType().equals(ChessPieceType.PAWN)) {
            throw new IllegalArgumentException("Pawn not supported");
        }
        List<ChessPos> piecePositions = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            if (board[i % BOARD_SIZE][i / BOARD_SIZE].equals(piece)) {
                piecePositions.add(new ChessPos(i % BOARD_SIZE, i / BOARD_SIZE));
            }
            if (piecePositions.size() >= 2) {
                break;
            }
        }
        return piecePositions;
    }

    private boolean isCapture(Move move) {
        int xFrom = move.from.getAsCoordinates()[0];
        int yFrom = move.from.getAsCoordinates()[1];
        int xTo = move.to.getAsCoordinates()[0];
        int yTo = move.to.getAsCoordinates()[1];

        return board[xFrom][yFrom].hasPiece() && board[xTo][yTo].hasPiece()
                && !board[xFrom][yFrom].getColor().equals(board[xTo][yTo].getColor());
    }

    private char getPieceRepresentation(ChessPiece piece) {
        char output;
        switch (piece.getPieceType()) {
            case KING:
                output = 'K';
                break;
            case QUEEN:
                output = 'Q';
                break;
            case ROOK:
                output = 'R';
                break;
            case BISHOP:
                output = 'B';
                break;
            case KNIGHT:
                output = 'N';
                break;
            case PAWN:
                output = 'P';
                break;
            default:
                output = '.';
                break;
        }
        if (piece.getColor().equals(ChessPieceColor.BLACK)) {
            output = Character.toLowerCase(output);
        }
        return output;
    }

    private String replaceNumbersWithEmptyFields(String input) {

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (Character.isDigit(current)) {
                int n = current - '0';
                output.append("E".repeat(Math.max(0, n)));
            } else if (current != '/') {
                output.append(current);
            }
        }
        return output.toString();
    }

    private boolean inRange(Integer... cords) {
        for (Integer cord : cords) {
            if (cord < 0 || cord >= BOARD_SIZE) {
                return false;
            }
        }
        return true;
    }
}