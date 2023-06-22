package model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChessEngine {
    private int level;
    private final PositionEvaluator positionEvaluator;
    private final GameBase gameBase;

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 6;

    public ChessEngine(int level) {
        gameBase = new GameBase();
        positionEvaluator = PositionEvaluator.getInstance();
        if (level >= MIN_LEVEL && level <= MAX_LEVEL) {
            this.level = level;
        } else {
            throw new IllegalArgumentException("This level's not a possibility");
        }
    }

    public void setLevel(int level) {
        if (level >= MIN_LEVEL && level <= MAX_LEVEL) {
            this.level = level;
        } else {
            throw new IllegalArgumentException("This level's not a possibility");
        }
    }

    public Move getMove(ChessBoard chessBoard, List<String> previousMoves) {
        List<String> previousMovesFiltered = previousMoves.stream()
                .map(move -> move.replaceAll("^\\d{1,2}\\.\\s*", ""))
                .collect(Collectors.toList());
        List<Pair<String, Integer>> moves = gameBase.getMoves(previousMovesFiltered);
        if (moves.isEmpty()) {
            return getMove(chessBoard);
        }
        String nextMoveString = RandomProbabilitySelector.selectRandomItem(moves);
        return chessBoard.getMoveFromString(nextMoveString);
    }

    public Move getMove(ChessBoard chessBoard) {

        List<Move> possibleMoves = chessBoard.getPossibleMoves();
        int bestMoveIndex = -1;
        if (chessBoard.getCurrentPlayer().equals(ChessPieceColor.WHITE)) {
            int bestValue = Integer.MIN_VALUE;
            for (int i = 0; i < possibleMoves.size(); i++) {
                Move move = possibleMoves.get(i);
                ChessBoard board = chessBoard.clone();
                board.movePiece(move);
                int minimaxValue = minimax(board, level, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (minimaxValue > bestValue) {
                    bestValue = minimaxValue;
                    bestMoveIndex = i;
                }
            }
        } else {
            int bestValue = Integer.MAX_VALUE;
            for (int i = 0; i < possibleMoves.size(); i++) {
                Move move = possibleMoves.get(i);
                ChessBoard board = chessBoard.clone();
                board.movePiece(move);
                int minimaxValue = minimax(board, level, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (minimaxValue < bestValue) {
                    bestValue = minimaxValue;
                    bestMoveIndex = i;
                }
            }
        }
        return possibleMoves.get(bestMoveIndex);
    }

    public int minimax(ChessBoard node, int depth, boolean isMaximizingPlayer, int alpha, int beta) {

        if (depth == 0) {
            return positionEvaluator.getEvaluation(node);
        }

        //noinspection IfStatementWithIdenticalBranches
        if (isMaximizingPlayer) {
            int bestVal = Integer.MIN_VALUE;
            for (ChessBoard childNode : getChildNodes(node)) {
                int value = minimax(childNode, depth - 1, false, alpha, beta);
                bestVal = Math.max(bestVal, value);
                alpha = Math.max(alpha, bestVal);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestVal;
        } else {
            int bestVal = Integer.MAX_VALUE;
            for (ChessBoard childNode : getChildNodes(node)) {
                int value = minimax(childNode, depth - 1, true, alpha, beta);
                bestVal = Math.min(bestVal, value);
                beta = Math.min(beta, bestVal);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestVal;
        }
    }

    private List<ChessBoard> getChildNodes(ChessBoard parentNode) {
        List<Move> possibleMoves = parentNode.getPossibleMoves();
        return possibleMoves.parallelStream()
                .map(move -> {
                    ChessBoard newBoard = parentNode.clone();
                    newBoard.movePiece(move);
                    return newBoard;
                })
                .collect(Collectors.toList());
    }
}
