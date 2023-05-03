package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessEngine {
    private int level;
    private PositionEvaluator positionEvaluator;

    private static int MIN_LEVEL = 1;
    private static int MAX_LEVEL = 6;

    public ChessEngine(int level) {
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

    public Move getMove(ChessBoard chessBoard) {
        List<Move> possibleMoves = chessBoard.getPossibleMoves();
        List<ChessBoard> possiblePositions = possibleMoves.parallelStream()
                .map(move -> {
                    ChessBoard newBoard = chessBoard.clone();
                    newBoard.movePiece(move);
                    return newBoard;
                })
                .collect(Collectors.toList());

        int bestMoveIndex = -1;
        if (chessBoard.getCurrentPlayer().equals(ChessPieceColor.WHITE)) {
            int bestValue = Integer.MIN_VALUE;
            for (int i = 0; i < possiblePositions.size(); i++) {
                ChessBoard board = possiblePositions.get(i);
                int minimaxValue = minimax(board, level, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                int evaluation = positionEvaluator.getEvaluation(board.getBoard()) + minimaxValue;
                if (evaluation > bestValue) {
                    bestValue = evaluation;
                    bestMoveIndex = i;
                }
            }
        } else {
            int bestValue = Integer.MAX_VALUE;
            for (int i = 0; i < possiblePositions.size(); i++) {
                ChessBoard board = possiblePositions.get(i);
                int minimaxValue = minimax(board, level, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
                int evaluation = positionEvaluator.getEvaluation(board.getBoard()) + minimaxValue;
                if (evaluation < bestValue) {
                    bestValue = evaluation;
                    bestMoveIndex = i;
                }
            }
        }
        return possibleMoves.get(bestMoveIndex);
    }

    public int minimax(ChessBoard node, int depth, boolean isMaximizingPlayer, int alpha, int beta) {

        if (depth == 0) {
            return positionEvaluator.getEvaluation(node.getBoard());
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
        List<ChessBoard> childNodes = new ArrayList<>();
        List<Move> possibleMoves = parentNode.getPossibleMoves();
        for (Move move : possibleMoves) {
            ChessBoard newBoard = parentNode.clone();
            newBoard.movePiece(move);
            childNodes.add(newBoard);
        }
        return childNodes;
    }


}
