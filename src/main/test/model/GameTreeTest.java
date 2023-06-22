package model;

import javafx.util.Pair;
import model.GameTree.GameTree;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameTreeTest {

    @Test
    void testGetMoves1() {
        GameTree gameTree = new GameTree();
        gameTree.addGame("1.e4 e5 2.f4 exf4 3.Bc4 d5");
        gameTree.addGame("1.e4 e5 2.Nf3 Nc6 3.d4 exd4");
        gameTree.addGame("1.e4 e5 2.Nf3 d6 3.d4 Nd7");
        gameTree.addGame("1.e4 e5 2.f4 exf4 3.Bc4 d5");
        gameTree.addGame("1.d4 Nf6 2.c4 g6 3.Nc3 Bg7");
        gameTree.addGame("1.e4 c5 2.Nf3 d6 3.d4 cxd4");

        List<Pair<String, Integer>> moves = gameTree.getMoves(new String[]{"e4", "e5"});

        assertTrue(moves.stream()
                .map(Pair::getKey)
                .collect(Collectors.toList()).containsAll(Arrays.asList("Nf3", "f4")));
        assertEquals(2, moves.size());
    }

    @Test
    void testGameBaseTime() {
        long start = System.currentTimeMillis();
        GameBase gameBase = new GameBase();
        long end = System.currentTimeMillis();

        assertTrue(end - start < 5000);
    }

    @Test
    void testGetMoves() {
        GameBase gameBase = new GameBase();
        System.out.println(gameBase.getMoves("e4", "e5"));
    }
}
