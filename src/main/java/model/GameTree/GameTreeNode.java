package model.GameTree;

import javafx.util.Pair;

import java.util.*;

public class GameTreeNode {

    private HashMap<String, GameTreeNode> childNodes;
    private String name;
    private int nChildNodes;

    public GameTreeNode(String name) {
        childNodes = new HashMap<>();
        this.name = name;
        nChildNodes = 0;
    }

    public void add(String[] gameMoves) {
        if (gameMoves.length > 0) {
            String first = gameMoves[0].replaceAll("^\\d{1,2}\\.?", "");;
            String[] rest = Arrays.copyOfRange(gameMoves, 1, gameMoves.length);
            if (childNodes.containsKey(first)) {
                GameTreeNode node = childNodes.get(first);
                node.add(rest);
            } else {
                GameTreeNode newNode = new GameTreeNode(first);
                childNodes.put(first, newNode);
                newNode.add(rest);
            }
        }
        nChildNodes += gameMoves.length;
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

    public int getNChildNodes() {
        return nChildNodes;
    }
}
