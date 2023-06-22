package model;

import javafx.util.Pair;
import model.GameTree.GameTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GameBase {

    private static final String PATH = "src/main/resources/grandmasterGames/";
    private static final List<String> fileNames = Arrays.asList("Adams", "Alekhine", "Anand",
            "Invanchuk", "Karpov", "Kasparov", "Kosteniuk", "Morozevich", "Shirov");
    private final GameTree gameTree;

    public GameBase() {
        gameTree = new GameTree();

        for (String grandmaster : fileNames) {
            try {
                List<String> games = importGames(PATH + grandmaster + ".txt");
                for (String game : games) {
                    gameTree.addGame(game);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error with games by " + grandmaster);
            }
        }
    }

    private List<String> importGames(String filePath) {
        List<String> games = new ArrayList<>();
        try {
            String contents = Files.readString(Paths.get(filePath));
            String[] gameStrings = contents.split("\r\n\r\n");
            for (int i = 0; i < gameStrings.length; i++) {
                if (i % 2 == 1) {
                    games.add(gameStrings[i].replaceAll("\r\n", ""));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return games;
    }

    public List<Pair<String, Integer>> getMoves(String... previousMoves) {
        return gameTree.getMoves(previousMoves);
    }

    public List<Pair<String, Integer>> getMoves(List<String> previousMoves) {
        return getMoves(previousMoves.toArray(new String[0]));
    }
}
