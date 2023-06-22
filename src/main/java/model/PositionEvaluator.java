package model;

import java.util.HashMap;
import java.util.Map;

public class PositionEvaluator {

    private static PositionEvaluator positionEvaluator;
    private static final int BOARD_SIZE = 8;

    private static final int valueFactor = 3;
    private static final int placementFactor = 1;

    public static final HashMap<ChessPieceType, Integer> pieceValues = new HashMap<>(Map.of(
            ChessPieceType.PAWN, 100,
            ChessPieceType.KNIGHT, 300,
            ChessPieceType.BISHOP, 300,
            ChessPieceType.ROOK, 500,
            ChessPieceType.QUEEN, 900,
            ChessPieceType.KING, Integer.MAX_VALUE / 10
    ));

    /*
    Bonus:
    A = excellentSquare
    B = good Square
    C = appropriate Square
    D = acceptable Square
    E = bad Square
    F = very bad Square
     */

    private static final String knightSquares = "FEDDDDEF/EDCCCCDE/DCBAABCD/DCAAAACD/DCAAAACD/DCBAABCD/EDCCCCDE/FEDDDDEF";
    private static final String bishopSquares = "FEEEEEEF/EDDDDDDE/EDCBBCDE/ECCBBCCE/EDABBADE/EBBBBBBE/EBDDDDBE/DEEEEEED";
    private static final String rookSquares = "DDDDDDDD/CAAAAAAC/FEEEEEEF/FEEEEEEF/FEEEEEEF/FEEEEEEF/FEEEEEEF/EEDCCDEE";
    private static final String pawnSquares = "EEEEEEEE/AAAAAAAA/DDCBBCDD/DDCBBCDD/DDDBBDDD/DEEDDEED/DCCFFCCD/EEEEEEEE";
    private static final String queenSquares = "FDDCCDDF/DBBBBBBD/DBAAAABD/CBAAAABC/BBAAAABB/EAAAAABE/EBAAABBE/FEEDDEEF";
    private static final String kingSquares = "EEEFFEEE/EEEFFEEE/DEEFFEED/DEEFFEED/CDDEEDDC/CDDDDDDC/BBDDDDBB/BACDDCAB";

    private static final HashMap<Character, Integer> BonusValues = new HashMap<>(Map.of(
            'A', 60,
            'B', 45,
            'C', 35,
            'D', 20,
            'E', 10,
            'F', 0
    ));

    @SuppressWarnings("FieldCanBeLocal")
    private static Character[][] knightMapW, bishopMapW, pawnMapW, kingMapW, queenMapW, rookMapW;
    @SuppressWarnings("FieldCanBeLocal")
    private static Character[][] knightMapB, bishopMapB, pawnMapB, kingMapB, queenMapB, rookMapB;

    private PositionEvaluator() {
        kingMapW = getPieceMap(kingSquares);
        queenMapW = getPieceMap(queenSquares);
        pawnMapW = getPieceMap(pawnSquares);
        rookMapW = getPieceMap(rookSquares);
        bishopMapW = getPieceMap(bishopSquares);
        knightMapW = getPieceMap(knightSquares);

        kingMapB = mirrorHorizontally(kingMapW);
        queenMapB = mirrorHorizontally(queenMapW);
        pawnMapB = mirrorHorizontally(pawnMapW);
        rookMapB = mirrorHorizontally(rookMapW);
        bishopMapB = mirrorHorizontally(bishopMapW);
        knightMapB = mirrorHorizontally(knightMapW);
    };

    public static PositionEvaluator getInstance() {
        if (positionEvaluator == null) {
            positionEvaluator = new PositionEvaluator();
        }
        return positionEvaluator;
    }

    public int getEvaluation(ChessBoard chessBoard) {
        ChessPiece[][] board = chessBoard.getBoard();
        int pieceValueEval = getPieceValueEval(board, ChessPieceColor.WHITE)
                - getPieceValueEval(board, ChessPieceColor.BLACK);
        int piecePlacementEval = getPiecePlacementEval(board, ChessPieceColor.WHITE)
                - getPiecePlacementEval(board, ChessPieceColor.BLACK);
        int winnerEval = getGameOverEval(chessBoard);
        if (winnerEval == 0) {
            return 0;
        }
        return pieceValueEval * valueFactor + piecePlacementEval * placementFactor + winnerEval;
    }

    private int getGameOverEval(ChessBoard chessBoard) {
        ChessWinner winner = chessBoard.getWinner();
        if (winner.equals(ChessWinner.WHITE)) {
            return Integer.MAX_VALUE / 10;
        } else if (winner.equals(ChessWinner.BLACK)) {
            return Integer.MIN_VALUE / 10;
        } else if (winner.equals(ChessWinner.DRAW) || winner.equals(ChessWinner.STALEMATE)) {
            return 0;
        }
        return -1;
    }

    private int getPieceValueEval(ChessPiece[][] board, ChessPieceColor player) {

        int pieceValueEval = 0;
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            if (board[i % BOARD_SIZE][i / BOARD_SIZE].getColor().equals(player)) {
                pieceValueEval += pieceValues.get(board[i % BOARD_SIZE][i / BOARD_SIZE].getPieceType());
            }
        }
        return pieceValueEval;
    }

    private int getPiecePlacementEval(ChessPiece[][] board, ChessPieceColor player) {

        int piecePlacementEval = 0;
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            if (board[i % BOARD_SIZE][i / BOARD_SIZE].getColor().equals(player)) {
                if (player.equals(ChessPieceColor.WHITE)) {
                    switch (board[i % BOARD_SIZE][i / BOARD_SIZE].getPieceType()) {
                        case PAWN:
                            piecePlacementEval += BonusValues.get(pawnMapW[i % BOARD_SIZE][i / BOARD_SIZE]);
                            break;
                        case KNIGHT:
                            piecePlacementEval += BonusValues.get(knightMapW[i % BOARD_SIZE][i / BOARD_SIZE]);
                            break;
                        case BISHOP:
                            piecePlacementEval += BonusValues.get(bishopMapW[i % BOARD_SIZE][i / BOARD_SIZE]);
                            break;
                        case ROOK:
                            piecePlacementEval += BonusValues.get(rookMapW[i % BOARD_SIZE][i / BOARD_SIZE]);
                            break;
                        case QUEEN:
                            piecePlacementEval += BonusValues.get(queenMapW[i % BOARD_SIZE][i / BOARD_SIZE]);
                            break;
                        case KING:
                            piecePlacementEval += BonusValues.get(kingMapW[i % BOARD_SIZE][i / BOARD_SIZE]);
                            break;
                        }
                    } else {
                        switch (board[i % BOARD_SIZE][i / BOARD_SIZE].getPieceType()) {
                            case PAWN:
                                piecePlacementEval += BonusValues.get(pawnMapB[i % BOARD_SIZE][i / BOARD_SIZE]);
                                break;
                            case KNIGHT:
                                piecePlacementEval += BonusValues.get(knightMapB[i % BOARD_SIZE][i / BOARD_SIZE]);
                                break;
                            case BISHOP:
                                piecePlacementEval += BonusValues.get(bishopMapB[i % BOARD_SIZE][i / BOARD_SIZE]);
                                break;
                            case ROOK:
                                piecePlacementEval += BonusValues.get(rookMapB[i % BOARD_SIZE][i / BOARD_SIZE]);
                                break;
                            case QUEEN:
                                piecePlacementEval += BonusValues.get(queenMapB[i % BOARD_SIZE][i / BOARD_SIZE]);
                                break;
                            case KING:
                                piecePlacementEval += BonusValues.get(kingMapB[i % BOARD_SIZE][i / BOARD_SIZE]);
                                break;
                        }
                    }
                }
            }
        return piecePlacementEval;
    }

    private Character[][] getPieceMap (String pieceSquares) {
        if (pieceSquares.length() != BOARD_SIZE * BOARD_SIZE + 7) {
            throw new IllegalStateException("This should not have happened");
        }
        pieceSquares = pieceSquares.replaceAll("/", "");
        Character[][] pieceMap = new Character[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            pieceMap[i % BOARD_SIZE][i / BOARD_SIZE] = pieceSquares.charAt(i);
        }
        return pieceMap;
    }

    private Character[][] mirrorHorizontally(Character[][] input) {
        Character[][] output = new Character[input[0].length][input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[input.length - i - 1];
        }
        return output;
    }
}
