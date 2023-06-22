package model.GameTree;

import javafx.util.Pair;

import java.util.*;

public class GameTree {

    private final HashMap<String, GameTreeNode> childNodes;

    private static final int MAX_NUMBER_MOVES = 10;

    public GameTree() {
        childNodes = new HashMap<>();
    }

    public void addGame(String gameMoves) {
        String[] moves = gameMoves.split(" ");
        if (moves.length < 5) {
            return;
        }
        String firstMove = moves[0].replaceAll("^\\d{1,2}\\.?", "");
        String[] updatedMoves = Arrays.copyOfRange(moves, 1, Math.min(moves.length - 1, MAX_NUMBER_MOVES));
        if (childNodes.containsKey(firstMove)) {
            GameTreeNode node = childNodes.get(firstMove);
            node.add(updatedMoves);
        } else {
            GameTreeNode newNode = new GameTreeNode(firstMove);
            newNode.add(updatedMoves);
            childNodes.put(firstMove, newNode);
        }
    }

    public List<Pair<String, Integer>> getMoves(String[] previousMoves) {
        if (previousMoves.length == 0) {
            ArrayList<Pair<String, Integer>> moves = new ArrayList<>();
            for (Map.Entry<String, GameTreeNode> entry : childNodes.entrySet()) {
                String moveString = entry.getKey();
                Integer moveFrequency = entry.getValue().getNChildNodes();
                moves.add(new Pair<>(moveString, moveFrequency));
            }
            return moves;
        }
        String first = previousMoves[0].replaceAll("^\\d{1,2}\\.?", "");
        String[] rest = Arrays.copyOfRange(previousMoves, 1, previousMoves.length);
        if (childNodes.containsKey(first)) {
            return childNodes.get(first).getMoves(rest);
        }
        return new ArrayList<>();
    }
}
